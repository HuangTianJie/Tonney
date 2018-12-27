package com.gop.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.cache.RedisCache;
import com.gop.common.CoinExchangeRateService;
import com.gop.common.dto.CoinExchangeRateEnum;
import com.gop.config.RedisConstants;
import com.gop.domain.TradeMatchResult;
import com.gop.mapper.TradeMatchResultMapper;
import com.gte.domain.dto.ExchangeRateDto;
import com.gte.mapper.valuation.CoinExchangeRateMapper;
/**
 * 
 * 
 * 定时更数字币的汇率
 */
@Slf4j
@Component("coinExchangeRateUpdateJob")
public class CoinExchangeRateUpdateJob implements SimpleJob{
    @Autowired
   private  RestTemplate restTemplate;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private CoinExchangeRateMapper coinExchangeRateMapper;
    @Autowired
    private TradeMatchResultMapper tradeMatchResultMapper;
	@Override
	public void execute(ShardingContext arg0) {
	System.out.println("更新汇率");
	Map<String,Object> exchangeRates = new HashMap<String,Object>();
//	获得所有汇率
	getAllExchangeRates(exchangeRates);
    redisCache.setObject(RedisConstants.MARKET_EXCHANGE_RATE, exchangeRates);
	
		
	}
	/**
	 * 获取私链和通政币对BTC和USD
	 */
	private void exchangeRate(List<ExchangeRateDto> exchangeRateDtos,Map<String,Object> hashMap){
		List<TradeMatchResult> selectPricesBySymbol = tradeMatchResultMapper.selectPricesBySymbol();
		for (int i = 0; i < selectPricesBySymbol.size(); i++) {
	         ExchangeRateDto exchangeRateDto = new ExchangeRateDto();
			TradeMatchResult tradeMatchResult = selectPricesBySymbol.get(i);
			BigDecimal price = tradeMatchResult.getPrice();
			String symbol = tradeMatchResult.getSymbol();
			String coinName = symbol.substring(symbol.indexOf("_")+1);
			exchangeRateDto.setExchangeRate(price);
			exchangeRateDto.setCreateTime(new Date());
			exchangeRateDto.setUpdateTime(new Date());
			exchangeRateDto.setCurrentCoin(coinName);
			exchangeRateDto.setTargetCoin("BTC");
			exchangeRateDtos.add(exchangeRateDto);
			hashMap.put(coinName, exchangeRateDto);
		}
		
		
	}
    /**
     * 获取不同币种对BTC和USD的汇率
     * @param coinId
     */
    private  void exchangeRate(String coinId,String name,Map<String,Object> hashMap,List<ExchangeRateDto> exchangeRateDtos){
      	 String url = new String("https://api.coinmarketcap.com/v2/ticker/{coinId}/?convert=BTC");
           String jsonStr =restTemplate.getForObject(url,String.class,coinId);
           JSONObject parseObject = JSON.parseObject(jsonStr);
           JSONObject data = parseObject.getJSONObject("data");
           JSONObject quotes = data.getJSONObject("quotes");
           JSONObject USDObject = quotes.getJSONObject("USD");
           BigDecimal exchangeRateUSD = USDObject.getBigDecimal("price");
           JSONObject BTCObject = quotes.getJSONObject("BTC");
           BigDecimal exchangeRateBTC = BTCObject.getBigDecimal("price");
           //只存入BTC跟USD的汇率
           if ("BTC".equals(name)) {
               ExchangeRateDto exchangeRateDto = new ExchangeRateDto();
               exchangeRateDto.setCreateTime(new Date());
               exchangeRateDto.setUpdateTime(new Date());
               exchangeRateDto.setCurrentCoin(name);
               exchangeRateDto.setTargetCoin("USD");
               exchangeRateDto.setExchangeRate(exchangeRateUSD);
               hashMap.put(name,exchangeRateDto);
               exchangeRateDtos.add(exchangeRateDto);
              
		   }else{
           //存入币种和BTC的汇率
           ExchangeRateDto exchangeRateDto2 = new ExchangeRateDto();
           exchangeRateDto2.setCreateTime(new Date());
           exchangeRateDto2.setUpdateTime(new Date());
           exchangeRateDto2.setCurrentCoin(name);
           exchangeRateDto2.setTargetCoin("BTC");
           exchangeRateDto2.setExchangeRate(exchangeRateBTC);
           exchangeRateDtos.add(exchangeRateDto2);
           hashMap.put(name,exchangeRateDto2);
		   }
      }
    /**
     * 获取USD对CNY的汇率
     * @param coinId
     */
    private void exchangeRate(Map<String,Object> hashMap,List<ExchangeRateDto> exchangeRateDtos){
      	 String url = new String("http://free.currencyconverterapi.com/api/v5/convert?q=USD_CNY&compact=y");
           String jsonStr =restTemplate.getForObject(url,String.class);
           JSONObject parseObject = JSON.parseObject(jsonStr);
           JSONObject data = parseObject.getJSONObject("USD_CNY");
           BigDecimal rate = data.getBigDecimal("val");
           //存入USD和CNY的汇率
           ExchangeRateDto exchangeRateDto = new ExchangeRateDto();
           exchangeRateDto.setCreateTime(new Date());
           exchangeRateDto.setUpdateTime(new Date());
           exchangeRateDto.setCurrentCoin("USD");
           exchangeRateDto.setTargetCoin("CNY");
           exchangeRateDto.setExchangeRate(rate);
           hashMap.put("USD",exchangeRateDto);
           exchangeRateDtos.add(exchangeRateDto);
           coinExchangeRateMapper.updateByCurrentCoins(exchangeRateDtos);
      }
    /**
     * 获取USD对CNY的汇率
     * 获取不同币种对BTC和USD的汇率
     */
    public void getAllExchangeRates(Map<String,Object> exchangeRates){
    	List<ExchangeRateDto> exchangeRateDtos = new ArrayList<ExchangeRateDto>();
    	//获取不同币种对BTC和USD的汇率
        for (CoinExchangeRateEnum coinExchangeRateEnum : CoinExchangeRateEnum.values()) {
      	  String code = coinExchangeRateEnum.getCode();
      	  String name = coinExchangeRateEnum.name();
      	  exchangeRate(code,name,exchangeRates,exchangeRateDtos);
  	  }
       	//获得私链汇率
     	exchangeRate(exchangeRateDtos,exchangeRates);
        //获取USD对CNY的汇率
     	exchangeRate(exchangeRates,exchangeRateDtos);
  
    }

}
