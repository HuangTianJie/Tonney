package com.gop.task.domestic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.gop.common.Environment;
import com.gop.common.Environment.EnvironmentEnum;
import com.gop.currency.transfer.service.WithdrawCurrencyService;
import com.gop.currency.withdraw.gateway.service.GetWayPayService;
import com.gop.currency.withdraw.gateway.service.cibpay.config.CibOrderStatus;
import com.gop.currency.withdraw.gateway.service.cibpay.service.CibPayService;
import com.gop.domain.ChannelCibOrderWithdraw;
import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.domain.enums.WithdrawCurrencyPayMode;
import com.gop.exception.AppException;


public class CibpayTask {
	private final Logger loger = LoggerFactory.getLogger(this.getClass());

	private final int limit = 30;

	@Autowired
	@Qualifier("cibPayService")
	private GetWayPayService<ChannelCibOrderWithdraw> cibOrderService;

	@Autowired
	@Qualifier("cibPayService")
	private CibPayService cibPayService;

	@Autowired
	private WithdrawCurrencyService withdrawOrderService;
	
	@Autowired
	private Environment environmentContxt;

	@Scheduled(cron = "0/10 * * * * ?")
	public void sendOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			Map<?, ?> returnMap = null;
			String returnStr = null;
	
			List<ChannelCibOrderWithdraw> cibOrders = cibOrderService.getOrdersByStatus(CibOrderStatus.INITIAL, limit);
			if (cibOrders == null || cibOrders.size() == 0) {
				return;
			}
			for (ChannelCibOrderWithdraw cibOrder : cibOrders) {
				loger.info("sendOrder: order:" + cibOrder);
				BigDecimal amount = cibOrder.getAmount().setScale(2, RoundingMode.DOWN);
				returnStr = cibPayService.cibPay(cibOrder.getPayNo(), cibOrder.getBankNo(), cibOrder.getAccountNo(),
						cibOrder.getAccountName(), cibOrder.getAccountType(), amount.toPlainString(),
						cibOrder.getTransferUsage());
				loger.info("sendOrder cibPay() return:{}", returnStr);
				if (returnStr == null) {
					loger.error("人民币转出:超级代付--支付--转帐id:{}, 提交订单失败,返回结果为空", cibOrder.getId());
					continue;
				}
				returnMap = JSON.parseObject(returnStr, Map.class);
				/*
				 * 当返回errcode为20101,即发送重复订单,否则该笔订单不会提交到收付直通车
				 */
				/*
				 * modify by lipeng. 20160903 交易结果判断标准： 交易成功：正常返回接口中的transStatus为1；
				 * 交易失败：正常返回接口中的transStatus为2；
				 * 其它情况（包括正常返回接口中的transStatus为3以及异常返回）建议都当作未决，
				 * 建议隔5分钟后调用单笔代付查询接口查询具体的交易结果。异常码含义详见5.异常码说明。
				 * 
				 * 所以交易有返回就把订单置为PROCESSING
				 */
				if (returnMap.containsKey("errcode")) {
					if (returnMap.get("errcode").equals("EPAY_20101")) {
						loger.info("重复订单发送,id{}", cibOrder.getId());
						cibOrder.setTransferStatus(CibOrderStatus.PROCESSING);
						// cibOrderService.updateCibOrder(cibOrder);
					} else {
						loger.info("人民币转出:超级代付--支付--转帐id:{}, 提交订单失败, 错误信息:{}", cibOrder.getId(),
								(String) returnMap.get("errmsg"));
						cibOrder.setTransferStatus(CibOrderStatus.FAILURE);
						cibOrder.setRemark((String) returnMap.get("errmsg"));
						cibOrderService.updateOrderByVersion(cibOrder);
						continue;
					}
				} else {
					cibOrder.setTransferStatus(CibOrderStatus.PROCESSING);
				}
				cibOrderService.updateOrder(cibOrder);
	
				loger.info("sendOrder: finish");
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ?")
	public void queryOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			Map<?, ?> returnMap = null;
			String transStatus = null;
			String returnStr = null;
	
			List<ChannelCibOrderWithdraw> cibOrders = cibOrderService
					.getOrdersByStatus(CibOrderStatus.PROCESSING.toString(), limit);
			if (cibOrders == null || cibOrders.size() == 0) {
				return;
			}
			for (ChannelCibOrderWithdraw cibOrder : cibOrders) {
	
				loger.info("queryOrder: order_id{}", cibOrder.getId());
				returnStr = cibPayService.cibQuery(cibOrder.getPayNo());
				loger.info("queryOrder pyQuery() return:{}", returnStr);
				if (returnStr == null) {
					continue;
				}
				returnMap = JSON.parseObject(returnStr, Map.class);
				if (returnMap == null || returnMap.size() == 0) {
					continue;
				}
	
				// 除transStatus为2或返回errcode=”EPAY_20102”（无匹配代付订单）外，其它结果建议继续把查询的订单当作交易未决处理
				if (returnMap.containsKey("errcode")) {
					if (returnMap.get("errcode").equals("EPAY_20102")) {
						loger.error("人民币转出:超级代付--支付--转帐id:{}, 支付失败, 错误信息:{}", cibOrder.getId(),
								(String) returnMap.get("errmsg"));
						/**
						 * 利用数据库中timestamp 字段存放第一次查询失败的时间,
						 * 除非在三十分钟之内一直查询失败，会将订单状态置为fail, 否则跳过
						 */
						if (null != cibOrder.getSysTimestamp() || "".equals(cibOrder.getSysTimestamp().trim())) {
							cibOrder.setSysTimestamp("" + System.currentTimeMillis());
							cibOrderService.updateOrder(cibOrder);
							continue;
						} else {
							long time = Long.valueOf(cibOrder.getSysTimestamp().trim());
							if ((time + 1000 * 60 * 30) < System.currentTimeMillis()) {
								continue;
							} else {
								transStatus = CibOrderStatus.FAILURE;
							}
						}
					} else {
						continue;
					}
				} else {
					transStatus = ((String) returnMap.get("transStatus")).trim();
				}
	
				/**
				 * 如果是处理中，跳过
				 */
				if (!CibOrderStatus.SUCCESS.equals(transStatus) && !CibOrderStatus.FAILURE.equals(transStatus)) {
					continue;
				}
				// transStatus = (String) returnMap.get("transStatus");
				// if (returnMap.containsKey("errcode") ) {
				// continue;
				// }else if( !TransactionStatus.SUCCESS.equals(transStatus) &&
				// !TransactionStatus.FAILURE.equals(transStatus) ){
				// continue;
				// }
				cibOrder.setTransferStatus(transStatus);
	
				if (returnMap.get("sno") != null) {
					cibOrder.setSerialNo((String) returnMap.get("sno"));
				}
				if (returnMap.get("transDate") != null) {
					cibOrder.setTransferDate((String) returnMap.get("transDate"));
				}
				if (returnMap.get("transTime") != null) {
					cibOrder.setTransferTime((String) returnMap.get("transTime"));
				}
				if (returnMap.get("transFee") != null) {
					cibOrder.setFee(new BigDecimal((String) returnMap.get("transFee")));
				}
	
				if (transStatus.equals(CibOrderStatus.FAILURE)) {
					cibOrder.setRemark((String) returnMap.get("remark"));
					cibOrderService.updateOrderByVersion(cibOrder);
				} else {
					cibOrder.setRemark("");
					if (returnMap.get("remark") != null) {
						cibOrder.setRemark((String) returnMap.get("remark"));
					}
					cibOrderService.updateOrder(cibOrder);
				}
			}
		}
	}

	@Scheduled(cron = "0/30 * * * * ?")
	public void updateOrder() {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			List<WithdrawCurrencyOrderUser> lstOrder = withdrawOrderService
					.getTransferCnyList(WithdrawCurrencyOrderStatus.PROCESSING, WithdrawCurrencyPayMode.SUPERPAY, limit);
	
			if (null == lstOrder || lstOrder.isEmpty())
				return;
			for (WithdrawCurrencyOrderUser order : lstOrder) {
	
				ChannelCibOrderWithdraw cibOrder = cibOrderService.getLastOrderByTxId(order.getInnerOrderNo());
				if (null == cibOrder) {
					loger.error("超级代付订单异常{}", "未发现超级代付的订单");
					order.setStatus(WithdrawCurrencyOrderStatus.UNKNOWN);
					order.setMsg("未发现超级代付订单");
					withdrawOrderService.toUnknown(order);
					continue;
				}
				if (cibOrder.getTransferStatus().equals(CibOrderStatus.SUCCESS)) {
					try {
						withdrawOrderService.confirm(order);
					} catch (AppException e) {
						loger.error("合众支付，确认成功时异常", e);
					}
				}
				if (cibOrder.getTransferStatus().equals(CibOrderStatus.FAILURE)) {
					try {
						order.setMsg(cibOrder.getRemark());
						withdrawOrderService.toUnknown(order);
					} catch (Exception e) {
						loger.error("超级支付，退款时异常", e);
					}
				}
			}
		}
	}

}
