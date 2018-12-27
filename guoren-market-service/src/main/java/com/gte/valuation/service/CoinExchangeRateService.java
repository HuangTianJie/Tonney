package com.gte.valuation.service;

import java.util.List;

import com.gte.domain.response.CoinExchangeRateResponse;

public interface CoinExchangeRateService {
    public List<CoinExchangeRateResponse> getCoinExchangeRates();
}
