package com.gop.common.dto;

import lombok.Getter;

public enum CoinExchangeRateEnum {

   BTC("1"),LTC("2"),ETH("1027"),BCH("1831"),USDT("825");
   @Getter
	private String code;
   CoinExchangeRateEnum(String code){
	   this.code=code;
   }
}
