package com.gte.valuation.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.code.consts.CommonCodeConst;
import com.gop.exception.AppException;
import com.gte.domain.dto.ExchangeRateDto;
import com.gte.domain.response.CoinExchangeRateResponse;
import com.gte.mapper.valuation.CoinExchangeRateMapper;
import com.gte.valuation.service.CoinExchangeRateService;
@Service
@Slf4j
public class CoinExchangeRateServiceImpl implements CoinExchangeRateService{
    @Autowired
    private CoinExchangeRateMapper coinExchangeRateMapper;
	@Override
	public List<CoinExchangeRateResponse> getCoinExchangeRates() {
		List<CoinExchangeRateResponse> responses = new ArrayList<CoinExchangeRateResponse>();
		BigDecimal BTC_USD = new BigDecimal(0);
		BigDecimal USD_CNY = new BigDecimal(0);
		List<ExchangeRateDto> coinExchangeRates = coinExchangeRateMapper.getCoinExchangeRates();
		if (coinExchangeRates==null) {
			log.error("CoinExchangeRateServiceImpl.getCNYValuationByAssetCode()报错查询不到汇率表数据");
			throw new AppException(CommonCodeConst.FIELD_ERROR,"数据异常");
		}
		for (int i = 0; i <coinExchangeRates.size(); i++) {
			BigDecimal coinName_BTC = new BigDecimal(0);
			CoinExchangeRateResponse response = new CoinExchangeRateResponse();
			ExchangeRateDto exchangeRateDto = coinExchangeRates.get(i);
			String currentCoin = exchangeRateDto.getCurrentCoin();
			BigDecimal exchangeRate = exchangeRateDto.getExchangeRate();
			response.setCoinName(currentCoin);
				if ("BTC".equals(currentCoin)) {
					BTC_USD =exchangeRate;
					response.setBTCExchangeRate(new BigDecimal(1));
				}
			    else if (currentCoin.equals("USD")) {
				    USD_CNY = exchangeRate;
				    continue;
			    }else{
			    	coinName_BTC = exchangeRate;
			    	response.setBTCExchangeRate(coinName_BTC);
			    }
				responses.add(response);
	
		}
		for (CoinExchangeRateResponse response : responses) {
			BigDecimal coinName_BTC = response.getBTCExchangeRate();
			BigDecimal coinName_USD = coinName_BTC.multiply(BTC_USD);
			BigDecimal coinName_CNY = coinName_USD.multiply(USD_CNY).setScale(2, BigDecimal.ROUND_HALF_UP);
			response.setUSDExchangeRate(coinName_USD);
			response.setCNYExchangeRate(coinName_CNY);
		}
		return responses;
	}

}
