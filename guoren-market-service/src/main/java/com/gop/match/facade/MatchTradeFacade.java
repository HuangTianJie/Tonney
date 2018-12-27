package com.gop.match.facade;

import com.gop.match.dto.FeeDto;

import java.math.BigDecimal;

public interface MatchTradeFacade {
	/**
	 * jsonObject = message; buyOrderNo = jsonObject.getString("buyTxno");
	 * CheckStateUtil.checkArgumentState(null == buyOrderNo, "buyOrderNo is
	 * null"); sellOrderNo = jsonObject.getString("sellTxno");
	 * CheckStateUtil.checkArgumentState(null == sellOrderNo, "sellOrderNo is
	 * null"); buyId = jsonObject.getInteger("buyId");
	 * CheckStateUtil.checkArgumentState(null == buyId, "buyId is null"); sellId
	 * = jsonObject.getInteger("sellId"); CheckStateUtil.checkArgumentState(null
	 * == sellId, "sellId is null"); num = jsonObject.getBigDecimal("num");
	 * CheckStateUtil.checkArgumentState(null == num, "num is null"); price =
	 * jsonObject.getBigDecimal("price"); CheckStateUtil.checkArgumentState(null
	 * == price, "price is null"); symbol = jsonObject.getString("symbol");
	 */
	public void matchRecord(String buyRequestNo, String sellRequestNo, Integer buyId, Integer sellId, BigDecimal num,
			BigDecimal price, String symbol);

	/**
	 * 获取买方手续费
	 * @param num 成交数量
	 * @param price 成交价格
	 * @param symbol 交易对
	 * @return 买方手续费
	 */
	public FeeDto getBuyFee(Integer buyUid, BigDecimal num, BigDecimal price, String symbol);

	/**
	 * 卖出手续费 费率
	 * @param num 成交数量
	 * @param price 成交价格
	 * @param symbol 交易对
	 * @return 卖出手续费
	 */
	public FeeDto getSellFee(Integer sellUid,BigDecimal num,  BigDecimal price,String symbol);
}
