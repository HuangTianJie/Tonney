package com.gop.c2c.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2cAdvertSellDto {
	//交易币种列表
	private List<String> assetcodeList;
	//国家列表
	private List<String> countryList;
	//货币列表
	private List<String> currencyList;
	//支付方式列表
	private List<C2cBasePayChannelDto> c2cBasePayChannelDto;
}
