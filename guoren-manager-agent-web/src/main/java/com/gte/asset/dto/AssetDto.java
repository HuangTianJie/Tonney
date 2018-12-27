package com.gte.asset.dto;

import com.gop.domain.Finance;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class AssetDto {
	private Integer userId;

	private String accountNo;

	private String assetCode;

	private BigDecimal amountAvailable;
	
	private BigDecimal amountLock;

	private BigDecimal tradeAmountTotal;  //交易总量

	private BigDecimal withdrawAmountTotal;  //提现总量

	public AssetDto(Finance finance,BigDecimal tradeAmountTotal,BigDecimal withdrawAmountTotal){
		
		this.userId = finance.getUid();
		this.accountNo= finance.getAccountNo();
		this.assetCode = finance.getAssetCode();
		this.amountAvailable = finance.getAmountAvailable();
		this.amountLock = finance.getAmountLock();
		this.tradeAmountTotal = tradeAmountTotal == null ? BigDecimal.ZERO:tradeAmountTotal;
		this.withdrawAmountTotal = withdrawAmountTotal == null ? BigDecimal.ZERO:withdrawAmountTotal;
	}
}
