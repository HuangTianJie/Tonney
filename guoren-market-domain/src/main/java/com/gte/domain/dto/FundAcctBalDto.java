package com.gte.domain.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

public class FundAcctBalDto {
	@Getter
	@Setter
	private String assetsCode;
	@Getter
	@Setter
	private BigDecimal amountAvailable;
	@Getter
	@Setter
	private BigDecimal amountLock;
	@Getter
	@Setter
	private BigDecimal amountTotal;
}
