package com.gop.common;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.asset.dto.AssetDto;
import com.gop.cache.RedisCache;
import com.gop.config.RedisConstants;
import com.gte.domain.dto.ExchangeRateDto;
import com.gte.mapper.valuation.CoinExchangeRateMapper;
@Service
public class CoinExchangeRateService {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private CoinExchangeRateMapper coinExchangeRateMapper;
	@SuppressWarnings("unchecked")
	public List<AssetDto> getExchangeRate(List<AssetDto> assetDtos) {
	
		Map<String,Object> exchangeRates = null;
		exchangeRates = (Map<String, Object>) redisCache.getObject(RedisConstants.MARKET_EXCHANGE_RATE);
		if (exchangeRates==null) {
			exchangeRates = new HashMap<String,Object>();
			@SuppressWarnings("unused")
			//查出汇率表各货币汇率存入Map
			List<ExchangeRateDto> coinExchangeRates = coinExchangeRateMapper.getCoinExchangeRates();
			for (int i = 0; i < coinExchangeRates.size(); i++) {
				ExchangeRateDto exchangeRateDto = coinExchangeRates.get(i);
				String currentCoin = exchangeRateDto.getCurrentCoin();
				exchangeRates.put(currentCoin, exchangeRateDto);
			}
			System.out.println("查出信息");
		}
		//查出通用汇率BTC跟USD的汇率，USD和CNY的汇率
		ExchangeRateDto exchangeRateDtoBTC_USD= (ExchangeRateDto)exchangeRates.get("BTC");
		BigDecimal exchangeRateBTC_USD = exchangeRateDtoBTC_USD.getExchangeRate();
		ExchangeRateDto exchangeRateDtoUSD_CNY = (ExchangeRateDto)exchangeRates.get("USD");
		BigDecimal exchangeRateUSD_CNY = exchangeRateDtoUSD_CNY.getExchangeRate();
        //遍历用户所有资产
    	for (int i = 0; i < assetDtos.size(); i++) {
    		AssetDto assetDto = assetDtos.get(i);
    		String assetCode = assetDto.getAssetCode();
    		BigDecimal amountAvailable = assetDto.getAmountAvailable();
    		BigDecimal amountLock = assetDto.getAmountLock();
    		BigDecimal amountTotal = amountLock.add(amountAvailable);//货币总金额
    		//查出对该币种与BTC的汇率
    		ExchangeRateDto exchangeRateDtoBTC = (ExchangeRateDto)exchangeRates.get(assetCode);
    		if ("CNY".equals(assetCode)) {
    			
        		BigDecimal CNY = amountTotal;
        		BigDecimal USD = CNY.divide(exchangeRateUSD_CNY,2,BigDecimal.ROUND_HALF_UP);
        		BigDecimal BTC = USD.divide(exchangeRateBTC_USD,8,BigDecimal.ROUND_HALF_UP);
        		CNY.setScale(2,BigDecimal.ROUND_HALF_UP);
        		assetDto.setAmountBTC(BTC);
        		assetDto.setAmountUSD(USD);
        		assetDto.setAmountCNY(CNY);
        		assetDto.setCoin("¥");
        		continue;
			}
    		if ("USD".equals(assetCode)) {
    	 		BigDecimal USD = amountTotal;
        		BigDecimal CNY = USD.multiply(exchangeRateUSD_CNY);
        		BigDecimal setScaleCNY = CNY.setScale(2,BigDecimal.ROUND_HALF_UP);
        		BigDecimal BTC = USD.divide(exchangeRateBTC_USD,8,BigDecimal.ROUND_HALF_UP);
        		CNY.setScale(2,BigDecimal.ROUND_HALF_UP);
        		assetDto.setAmountBTC(BTC);
        		assetDto.setAmountUSD(USD);
        		assetDto.setAmountCNY(setScaleCNY);
        		assetDto.setCoin("$");
        		continue;
			}
    		if ("BTC".equals(assetCode)) {
    			
    			exchangeRateDtoBTC = new ExchangeRateDto();
    			exchangeRateDtoBTC.setExchangeRate(new BigDecimal("1"));
			}
    		
    		if ("SGD".equals(assetCode)) {
    			assetDto.setCoin("S$");
			}
    		if ("TWD".equals(assetCode)) {
    			assetDto.setCoin("NT$");
			}
    		
    		if (exchangeRateDtoBTC!=null) {
    		BigDecimal exchangeRateBTC = exchangeRateDtoBTC.getExchangeRate();
    		BigDecimal BTC = amountTotal.multiply(exchangeRateBTC);
    		BigDecimal USD = BTC.multiply(exchangeRateBTC_USD);
    		BigDecimal CNY = USD.multiply(exchangeRateUSD_CNY);
      		BigDecimal setScaleCNY = CNY.setScale(2,BigDecimal.ROUND_HALF_UP);
    		BigDecimal setScaleUSD = USD.setScale(2,BigDecimal.ROUND_HALF_UP);
    		BigDecimal setScaleBTC = BTC.setScale(8,BigDecimal.ROUND_HALF_UP);
    		assetDto.setAmountBTC(setScaleBTC);
    		assetDto.setAmountUSD(setScaleUSD);
    		assetDto.setAmountCNY(setScaleCNY);
		}
 

		}
		return assetDtos;
    }
   
}
