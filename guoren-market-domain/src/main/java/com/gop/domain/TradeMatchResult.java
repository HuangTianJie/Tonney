package com.gop.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeMatchResult {
	private Long id;

	/**
	 * 交易对
	 */
	private String symbol;

	/**
	 * 买入委托
	 */
	private String buyInnerOrderNo;

	/**
	 * 卖出委托
	 */
	private String sellInnerOrderNo;

	/**
	 * 买方券商id
	 */
	private Integer buyBrokerId;

	/**
	 * 卖方券商id
	 */
	private Integer sellBrokerId;

	/**
	 * 买入成交流水号：用于资产增减的唯一编号
	 */
	private String buyRequestNo;

	/**
	 * 卖出成交流水号：用于资产增减的唯一编号
	 */
	private String sellRequestNo;

	/**
	 * 成交数量
	 */
	private BigDecimal number;

	/**
	 * 成交价格
	 */
	private BigDecimal price;

	/**
	 * 买方用户id
	 */
	private Integer buyUid;

	/**
	 * 卖方用户id
	 */
	private Integer sellUid;

	/**
	 * 买入手续费
	 */
	private BigDecimal buyFee;

	/**
	 * 买入手续费-资产代码
	 */
	private String  buyFeeAssetCode;
	/**
	 * 卖出手续费
	 */
	private BigDecimal sellFee;

	/**
	 * 卖出手续费-资产代码
	 */
	private String  sellFeeAssetCode;

	private Date createTime;

}