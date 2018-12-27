package com.gop.c2c.dto;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
@Data
public class C2cBankDto {

	//银行
	@NotBlank
	private String bank;
	//支行
	private String subBank;
	//姓名
	@NotBlank
	private String name;
	//银行卡号
	@NotBlank
	private String acnumber;
	
}
