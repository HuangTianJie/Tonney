package com.gop.c2c.dto;



import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2cBuyAdvertEditDto {
	//交易币种列表
	private List<String> assetcodeList;
	//国家列表
	private List<String> countryList;
	//货币列表
	private List<String> currencyList;
	//交易币种
	private String assetcode;
	//国家
	private String country;
	//货币
	private String currency;
	//交易价格
	private BigDecimal tradePrice;
	//预计购买
	private BigDecimal buyPrice;
	//备注信息
	private String remark;
	
}
