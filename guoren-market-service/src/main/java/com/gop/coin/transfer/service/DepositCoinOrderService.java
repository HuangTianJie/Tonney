package com.gop.coin.transfer.service;

import java.math.BigDecimal;

public interface DepositCoinOrderService {

	/**
	 * 
	 * @param assetCode
	 * @param fromAddress
	 * @param toAddress
	 * @param amount
	 * @param outerOrder
	 * @param sendMessage
	 * @return 订单流水号
	 */
	public void coinDepositOrder(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage);

	public void depositConfirm(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage);

	public void depositCancel(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage);

	/**
	 * 提交充值并直接审核 该方法只适用商户充值
	 * 
	 * @param uid
	 * @param outOrder
	 * @param assetCode
	 * @param amount
	 * @param fee
	 * @param address
	 * @param message
	 */
	public void coinDepositOrderUnCerf(int uid, String outOrder, String assetCode, BigDecimal amount);


}
