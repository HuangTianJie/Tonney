package com.gte.exchange.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gte.domain.response.CoinExchangeRateResponse;
import com.gte.valuation.service.CoinExchangeRateService;
@RestController
@Api("数字币汇率")
public class CoinExchangeRateController {
	/**
	 * 获取数字币对CNY,USD的汇率
	 */
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;
    
    @GetMapping("/getCoinExchangeRates")
    @ApiOperation(value = "获取数字币对CNY,USD的汇率", notes = "获取数字币对CNY,USD的汇率")
	public List<CoinExchangeRateResponse> getCoinExchangeRates(@AuthForHeader AuthContext context){
    	List<CoinExchangeRateResponse> responses= coinExchangeRateService.getCoinExchangeRates();
	    return responses;
    }

}
