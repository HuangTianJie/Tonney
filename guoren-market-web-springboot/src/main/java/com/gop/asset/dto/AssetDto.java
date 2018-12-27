package com.gop.asset.dto;

import java.math.BigDecimal;

import com.gop.domain.Finance;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AssetDto {
	private Integer userId;

	private String accountNo;

	private String assetCode;

	private BigDecimal amountAvailable;
	
	private BigDecimal amountLock;

	private BigDecimal amountBTC;
	
	private BigDecimal amountUSD;
	
	private BigDecimal amountCNY;
	private String coin;
	public AssetDto(Finance finance){
		
		this.userId = finance.getUid();
		this.accountNo= finance.getAccountNo();
		this.assetCode = finance.getAssetCode();
		this.amountAvailable = finance.getAmountAvailable();
		this.amountLock = finance.getAmountLock();
	}
}
