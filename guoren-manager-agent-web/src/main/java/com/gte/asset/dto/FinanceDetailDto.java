package com.gte.asset.dto;

import com.gop.domain.FinanceDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FinanceDetailDto {

	Date createDate;

	BigDecimal amount;

	String assetCode;

	public FinanceDetailDto(FinanceDetail detail) {

		this.createDate = detail.getCreateDate();

		this.amount = detail.getAmountAvailable();
		this.assetCode = detail.getAssetCode();
	}

}
