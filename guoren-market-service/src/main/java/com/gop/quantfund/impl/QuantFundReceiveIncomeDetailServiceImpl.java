package com.gop.quantfund.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.QuantFundReceiveIncomeDetail;
import com.gop.mapper.QuantFundReceiveIncomeDetailMapper;
import com.gop.quantfund.QuantFundReceiveIncomeDetailService;

@Service
public class QuantFundReceiveIncomeDetailServiceImpl implements QuantFundReceiveIncomeDetailService{
	
	@Autowired
	private QuantFundReceiveIncomeDetailMapper  quantFundReceiveIncomeDetailMapper;
	
	@Override
	public void addQuantFundReceiveIncomeDetail(QuantFundReceiveIncomeDetail quantFundReceiveIncomeDetail) {
		quantFundReceiveIncomeDetailMapper.addQuantFundReceiveIncomeDetail(quantFundReceiveIncomeDetail);
	}

}
