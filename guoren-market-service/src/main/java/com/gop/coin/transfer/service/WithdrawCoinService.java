package com.gop.coin.transfer.service;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.enums.WithdrawCoinOrderStatus;

public interface WithdrawCoinService {

	/**
	 * 提交订单并直接审核 该方法只适用商户充值
	 * 
	 * @param uid
	 * @param outOrder
	 * @param assetCode
	 * @param amount
	 * @param fee
	 * @param address
	 * @param message
	 */
	public void withdrawCoinOrderUnCerf(int uid, String outOrder, String assetCode, BigDecimal amount);

	/**
	 * 提交订单
	 * 
	 * @param uid
	 * @param outOrder
	 * @param assetCode
	 * @param amount
	 * @param fee
	 * @param address
	 * @param message
	 */
	public void withdrawCoinOrder(int uid, String outOrder, String assetCode, BigDecimal amount, BigDecimal fee,
			String address, String message);

	/**
	 * 审核通过
	 * 
	 * @param id
	 * @param adminId
	 */
	public void withdraw(int id, int adminId);

	/**
	 * 退款
	 * 
	 * @param id
	 * @param adminId
	 */
	public void withdrawRefund(int id, int adminId, String refuseMs);

	/**
	 * 提现成功回调确认
	 * 
	 * @param txid
	 */
	public void withdrawConfirm(String txid,BigDecimal chainFee);

	/**
	 * 提现失败处理确认
	 * 
	 * @param txid
	 * @param msg
	 *            TODO
	 */
	public void withdrawFail(String txid, String msg);

	/**
	 * 获取提现手续费
	 * 
	 * @param txid
	 * @param msg
	 *            TODO
	 * @return TODO
	 */
	public BigDecimal getWithdrawFee(BigDecimal amount);
	
	/**
	 * 获取用户每日累计已提币数量
	 * 
	 */
	public BigDecimal getUserDailyWithdrawedCoinValue(Integer uid,String assetCode,Date beginDate,Date endDate);

	/**
	 * 查询对应币种指定状态的数量
	 * @param assetCode
	 * @param status
	 * @return
	 */
	public Integer getAmountOfAssetWithStatus(String assetCode, WithdrawCoinOrderStatus status);

}
