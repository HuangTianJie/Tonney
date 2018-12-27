package com.gte.mapper.valuation;

import java.util.List;

import com.gte.domain.dto.ExchangeRateDto;

public interface CoinExchangeRateMapper {

	List<ExchangeRateDto> getCoinExchangeRates();
	void insertCoinExchangeRate(ExchangeRateDto exchangeRateDto);
	void updateByCurrentCoins(List<ExchangeRateDto> exchangeRateDtos);
	
}
