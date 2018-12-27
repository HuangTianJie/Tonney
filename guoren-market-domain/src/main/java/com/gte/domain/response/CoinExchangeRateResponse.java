package com.gte.domain.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

public class CoinExchangeRateResponse {
	@Getter
	@Setter
	private String coinName;
	@Getter
	@Setter
	private BigDecimal BTCExchangeRate;
	@Getter
	@Setter
	private BigDecimal CNYExchangeRate;
	@Getter
	@Setter
	private BigDecimal USDExchangeRate;
}
