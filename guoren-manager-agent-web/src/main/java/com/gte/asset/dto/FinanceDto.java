package com.gte.asset.dto;

import com.gop.domain.Finance;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceDto {
	
	Integer uid;
	
	String assetCode;
	
	BigDecimal availiable;
	
	BigDecimal lock;
	
	BigDecimal loan;
	
	public FinanceDto(Finance finance){
		
		this.uid = finance.getUid();
		
		this.assetCode = finance.getAssetCode();
		
		this.availiable = finance.getAmountAvailable();
		
		this.lock = finance.getAmountLock();
		
		this.loan = finance.getAmountLoan();
		
	}
	
	
}
