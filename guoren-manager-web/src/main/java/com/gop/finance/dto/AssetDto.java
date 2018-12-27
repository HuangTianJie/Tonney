package com.gop.finance.dto;

import java.math.BigDecimal;

import com.gop.domain.Finance;

import com.gop.domain.User;
import com.gop.domain.UserBeginingBalance;
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

	private BigDecimal amountTotal;

	public AssetDto(Finance finance, String email){
		this.userId = finance.getUid();
		if(email != null) {
			this.accountNo = email;
		}else{
			this.accountNo = "用户不存在";
		}
		this.assetCode = finance.getAssetCode();
		this.amountAvailable = finance.getAmountAvailable();
		this.amountLock = finance.getAmountLock();
		this.amountTotal = finance.getAmountAvailable().add(finance.getAmountLock());
	}

	public AssetDto(UserBeginingBalance userBeginingBalance){
		this.userId = userBeginingBalance.getUid();
		if(userBeginingBalance.getAccountNo() != null) {
			this.accountNo = userBeginingBalance.getAccountNo();
		}else{
			this.accountNo = "用户不存在";
		}
		this.assetCode = userBeginingBalance.getAssetCode();
		this.amountAvailable = userBeginingBalance.getAmount_available();
		this.amountLock = userBeginingBalance.getAmount_lock();
		this.amountTotal = userBeginingBalance.getAmount_total();
	}
}
