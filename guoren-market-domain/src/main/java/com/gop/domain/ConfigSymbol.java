package com.gop.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.enums.SymbolStatus;

import lombok.Data;

/*
 * code1现金，code2数字资产
 * */
@Data
public class ConfigSymbol {
	private Integer id;

	private String symbol;

	/**
	 * 计价币（买的币种）
	 */
	private String assetCode1;

	/**
	 * 交易币（卖的币种）
	 */
	private String assetCode2;

	private SymbolStatus status;

	private String title;

	private String name;

	private String description;

	private BigDecimal minAmount1;

	private BigDecimal minAmount2;

	private BigDecimal maxAmount1;

	private BigDecimal maxAmount2;
	
	private Integer minPrecision1;

	private Integer minPrecision2;
	
	private BigDecimal assetCode1Fee;

	private BigDecimal assetCode2Fee;

	private Date createDate;

	private Date updateDate;

}