package com.gop.task.domestic;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.gop.currency.withdraw.gateway.service.ulpay.service.UlPayService;
import com.gop.domain.ChannelUlpayOrderWithdraw;
import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.UlPayStatus;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.domain.enums.WithdrawCurrencyPayMode;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class UlpayTask {
	
	private final int limit = 30;

	@Autowired
	@Qualifier("ulPayService")
	private GetWayPayService<ChannelUlpayOrderWithdraw> ulpayOrderService;

	@Autowired
	@Qualifier("ulPayService")
	private UlPayService ulPayService;

	@Autowired
	private WithdrawCurrencyService withdrawOrderService;
	
	@Autowired
	private Environment environmentContxt;

	@Scheduled(cron = "0/10 * * * * ? ")
	@Transactional
	public void sendOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			Map<String, Object> ret = new HashMap<>();
			List<ChannelUlpayOrderWithdraw> ulpayOrders = ulpayOrderService
					.getOrdersByStatus(UlPayStatus.INITIAL.toString(), 30);
			if (ulpayOrders == null || ulpayOrders.size() == 0) {
				return;
			}
			for (ChannelUlpayOrderWithdraw order : ulpayOrders) {
				
				try {
					order = ulpayOrderService.getOrderForUpdate(order.getNo());
				} catch (Exception e) {
					log.error("获取订单锁资源失败", e);
					log.info("获取order:{}锁资源失败", order.getTxNo());
					continue;
				}
	
				log.info("sendOrder: order:" + order);
				try {
					ret = ulPayService.generalBatchPay(order.getReqSn(), order.getPayNo(), order.getBankName(),
							order.getAccountNo(), order.getBankCode(), order.getAccountName(), order.getAmount(),
							order.getRemark());
				}  catch (AppException e) {
					ret.put("retCode", "0003");// 如果支付异常，状态代码设置为未知
					ret.put("retMsg", e.getMessage());
					log.info("合众易宝支付失败:" + e.getMessage());
				}
	
				if ((Boolean) ret.get("retStatus")) {
					log.info("订单支付成功 ： " + order.getTxNo());
					order.setStatus(UlPayStatus.PROCESSING);
					order.setUpdateDate(new Timestamp(System.currentTimeMillis()));
					ulpayOrderService.updateOrder(order);
					log.info("合众 支付成功，订单状态被更改 : " + order.getStatus());
				} else {
					// 订单重复的时候会返回码为0003，并且返回重复订单号订单号的信息
					if (null != ret.get("retCode") && ret.get("retCode").equals("0003")
							&& ret.get("retMsg").toString().contains("重复订单号")) {
						log.info("订单重复发送 :  " + order.getTxNo());
						order.setStatus(UlPayStatus.PROCESSING);
						order.setUpdateDate(new Timestamp(System.currentTimeMillis()));
						ulpayOrderService.updateOrder(order);
						log.info("合众订单重复，订单状态被更改 : " + order.getStatus());
					} else if (null != ret.get("retCode")) {
						log.info("订单支付失败 : " + order.getTxNo());
						order.setStatus(UlPayStatus.FAILURE);
						order.setUpdateDate(new Timestamp(System.currentTimeMillis()));
						order.setRemark(
								"0:" + ret.get("retCode").toString().trim() + ":" + ret.get("retMsg").toString().trim());
						ulpayOrderService.updateOrderByVersion(order);
						log.info("合众 支付失败，订单状态被更改 : " + order.getStatus());
					}
				}
	
				log.info("一笔订单处理结束....");
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ? ")
	@Transactional
	public void queryOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			List<ChannelUlpayOrderWithdraw> lst = ulpayOrderService.getOrdersByStatus(UlPayStatus.PROCESSING.toString(), limit);
	
			for (ChannelUlpayOrderWithdraw order : lst) {
				Map<String, Object> ret = ulPayService.generalBatchQuery(order.getReqSn(), order.getPayNo());
				if ((Boolean) ret.get("retStatus")) {
					try{
						order = ulpayOrderService.getOrderForUpdate(order.getNo());
					}catch (Exception e) {
						//如果有其他用户在竞争锁资源，则任务终止
						log.info("获取用户订单被锁定，获取锁资源失败");
						break;
					}
					
					if ("0000".equals(ret.get("retDetailCode").toString().trim())) {
						log.info("订单支付成功 ： " + order.getTxNo());
						order.setStatus(UlPayStatus.SUCCESS);
						order.setUpdateDate(new Timestamp(System.currentTimeMillis()));
						ulpayOrderService.updateOrder(order);
					} else if (!"0002".equals(ret.get("retDetailCode").toString().trim())) {
						log.info("订单支付失败 :  " + order.getTxNo());
						order.setStatus(UlPayStatus.FAILURE);
						order.setRemark("1:" + ret.get("retDetailCode").toString().trim() + ":"
								+ ret.get("retMsg").toString().trim());
						order.setUpdateDate(new Timestamp(System.currentTimeMillis()));
						ulpayOrderService.updateOrderByVersion(order);
					}
				} else {
					// 0002 表示状态未知,0000表示订单成功，
					log.info("订单查询失败 : " + order.getTxNo());
				}
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ? ")
	@Transactional
	public void updateOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			List<WithdrawCurrencyOrderUser> lstOrder = withdrawOrderService.getTransferCnyList(WithdrawCurrencyOrderStatus.PROCESSING,
					WithdrawCurrencyPayMode.ULPAY, limit);
	
			if (null == lstOrder || lstOrder.isEmpty())
				return;
			for (WithdrawCurrencyOrderUser order : lstOrder) {
	
				ChannelUlpayOrderWithdraw record = ulpayOrderService.getLastOrderByTxId(order.getInnerOrderNo());
				if (null == record){
					log.error("超级代付订单异常{}", order.getInnerOrderNo());
					throw new AppException(CommonCodeConst.SERVICE_ERROR);
				}
				if (record.getStatus().equals(UlPayStatus.SUCCESS)) {
					try {
						withdrawOrderService.confirm(order);
					} catch (AppException e) {
						log.error("合众支付，确认成功时异常", e);
					}
				}
				
				if (record.getStatus().equals(UlPayStatus.FAILURE)) {
					try {
						if (!order.getStatus().equals(WithdrawCurrencyOrderStatus.PROCESSING)) {
							throw new AppException(CommonCodeConst.SERVICE_ERROR, "转帐记录状态错误");
						}
						order.setMsg(record.getRemark());
						withdrawOrderService.toUnknown(order);
						
					} catch (Exception e) {
						log.error("合众支付失败,orderid : {}，更新订单状态时异常", order.getInnerOrderNo(), e);
					}
				}
			}
		}
	}
	
}
