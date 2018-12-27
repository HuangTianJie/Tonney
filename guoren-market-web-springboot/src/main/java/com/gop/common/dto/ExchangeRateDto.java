package com.gop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class ExchangeRateDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Getter
	@Setter
	private String currentCoin;
	@Getter
	@Setter
	private String targetCoin;
	@Getter
	@Setter
	private BigDecimal exchangeRate;

	@Getter
	@Setter
	private Date createTime;
	@Getter
	@Setter
	private Date updateTime;

}
