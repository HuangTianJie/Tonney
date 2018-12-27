package com.gop.c2c.dto;



import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.mode.vo.BaseDto;

import lombok.Data;

@Data
public class C2cBuyAdvertDeployDto extends BaseDto{
	@NotBlank
	//交易币种
	private String assetcode;
	
	@NotBlank
	//国家
	private String country;
	
	@NotBlank
	//货币
	private String currency;
	
	@NotNull
	//交易价格
	private BigDecimal tradePrice;
	
	@NotNull
	//预计购买
	private BigDecimal buyPrice;
	
	//备注信息
	private String remark;
	
	@NotBlank
	//广告ID
	private String advertId;
	
}
