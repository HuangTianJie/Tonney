package com.gop.c2c.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class C2cSellAdvertEditDto {
	//交易币种列表
	private List<String> assetcodeList;
	//国家列表
	private List<String> countryList;
	//货币列表
	private List<String> currencyList;
	//昵称
	private String nickName; 
	//交易币种
	private String assetcode;
	//国家
	private String country;
	//货币
	private String currency;
	//手机号
	private String phone;
	//交易价格
	private BigDecimal tradePrice; 
	//最小售出个数
	private BigDecimal minAmount; 
	//最大售出个数
	private BigDecimal maxAmount;
	//备注信息
	private String remark;

}
