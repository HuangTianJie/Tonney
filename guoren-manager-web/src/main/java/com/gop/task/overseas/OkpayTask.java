package com.gop.task.overseas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gop.code.consts.CommonCodeConst;
import com.gop.common.Environment;
import com.gop.common.Environment.EnvironmentEnum;
import com.gop.currency.transfer.service.WithdrawCurrencyService;
import com.gop.currency.withdraw.gateway.service.GetWayPayService;
import com.gop.currency.withdraw.gateway.service.okpay.dto.entity.OperationStatus;
import com.gop.currency.withdraw.gateway.service.okpay.dto.entity.TransactionInfo;
import com.gop.currency.withdraw.gateway.service.okpay.service.OkPayService;
import com.gop.domain.ChannelOkpayOrderWithdraw;
import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.OkpayStatus;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.domain.enums.WithdrawCurrencyPayMode;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OkpayTask {
	
	private final int limit = 30;

	@Autowired
	@Qualifier("okPayService")
	private GetWayPayService<ChannelOkpayOrderWithdraw> okpayOrderService;

	@Autowired
	@Qualifier("okPayService")
	private OkPayService okPayService;

	@Autowired
	private WithdrawCurrencyService withdrawOrderService;
	
	@Autowired
	private Environment environmentContxt;

	@Scheduled(cron = "0/30 * * * * ? ")
	@Transactional
	public void sendOrder() {
		if (EnvironmentEnum.US.equals(environmentContxt.getSystemEnvironMent())) {
			TransactionInfo info = null;
			List<ChannelOkpayOrderWithdraw> okpayOrders = okpayOrderService
					.getOrdersByStatus(OkpayStatus.INITIAL.toString(), 30);
			if (okpayOrders == null || okpayOrders.size() == 0) {
				return;
			}
			for (ChannelOkpayOrderWithdraw order : okpayOrders) {
				try {
					order = okpayOrderService.getOrderForUpdate(order.getId());
				} catch (Exception e) {
					log.error("获取订单锁资源失败", e);
					log.info("获取order:{}锁资源失败", order.getTxid());
					continue;
				}
	
				log.info("sendOrder: order:" + order);
				try {
					info = okPayService.pay(order.getAccountNo(), order.getCurrencyCode(), order.getAmount(),
							order.getTransferUsage(), order.getPayNo());
					log.info("sendOrder okPay() return:{}", info);
					if (info == null) {
						log.error("人民币转出:OKPAY--支付--转帐id:{}, 提交订单失败,返回结果为空", order.getId());
						continue;
					}
					if (info.getComment().equals("Duplicate_Payment")
							|| info.getStatus().equals(OperationStatus.Completed)) {
						order.setTransferStatus(OkpayStatus.PROCESSING);
						order.setSerialNo("" + info.getID());
					} else {
						order.setTransferStatus(OkpayStatus.FAILURE);
					}
				} catch (AppException e) {
					log.info("okpay支付业务异常");
					order.setTransferStatus(OkpayStatus.FAILURE);
				} catch (Exception e) {
					log.error("okpay 支付失败 : " + e);
					continue;
				}
	
				okpayOrderService.updateOrder(order);
	
				log.info("sendOrder: finish");
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ? ")
	@Transactional
	public void queryOrder() {
		if (EnvironmentEnum.US.equals(environmentContxt.getSystemEnvironMent())) {
			TransactionInfo info = null;
			List<ChannelOkpayOrderWithdraw> orders = okpayOrderService.getOrdersByStatus(OkpayStatus.PROCESSING.toString(),
					limit);
			if (orders == null || orders.size() == 0) {
				return;
			}
			for (ChannelOkpayOrderWithdraw record : orders) {
	
				log.info("queryOrder: order_id{}", record.getId());
				try {
					info = okPayService.query(Long.valueOf(record.getSerialNo()), record.getPayNo());
					log.info("queryOrder pyQuery() return:{}", info);
					if (info == null) {
						continue;
					}
					if (info.getStatus().equals(OperationStatus.Completed)) {
						record.setTransferStatus(OkpayStatus.SUCCESS);
					} else if (info.getStatus().equals(OperationStatus.Error)) {
						record.setTransferStatus(OkpayStatus.FAILURE);
					} else {
						continue;
					}
					okpayOrderService.updateOrder(record);
				} catch (Exception e) {
					log.error("okpay 查询业务失败 : " + e);
					continue;
				}
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ? ")
	@Transactional
	public void updateIndivualOrder() {
		if (EnvironmentEnum.US.equals(environmentContxt.getSystemEnvironMent())) {
			List<WithdrawCurrencyOrderUser> lstOrder = withdrawOrderService.getTransferCnyList(WithdrawCurrencyOrderStatus.PROCESSING,
					WithdrawCurrencyPayMode.OKPAY, limit);
	
			if (null == lstOrder || lstOrder.isEmpty())
				return;
			for (WithdrawCurrencyOrderUser order : lstOrder) {
	
				ChannelOkpayOrderWithdraw record = okpayOrderService.getLastOrderByTxId(order.getInnerOrderNo());
				if (null == record)
					log.error("超级代付订单异常{}", order.getInnerOrderNo());
	
				if (record.getTransferStatus().equals(OkpayStatus.SUCCESS)) {
					try {
						withdrawOrderService.confirm(order);
					} catch (AppException e) {
						log.error("okpay支付，确认成功时异常", e);
					}
				}
				
				if (record.getTransferStatus().equals(OkpayStatus.FAILURE)) {
					try {
						if (!order.getStatus().equals(WithdrawCurrencyOrderStatus.PROCESSING)) {
							throw new AppException(CommonCodeConst.SERVICE_ERROR, "转帐记录状态错误");
						}
						withdrawOrderService.toUnknown(order);
						
					} catch (Exception e) {
						log.error("okpay支付失败,orderid : {}，更新订单状态时异常", order.getInnerOrderNo(), e);
					}
				}
			}
		}
	}

}
