package com.gop.quantfund.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.QuantFundTicketPreReturnTemp;
import com.gop.mapper.QuantFundTicketPreReturnTempMapper;
import com.gop.quantfund.QuantFundTicketPreReturnTempService;

@Service
public class QuantFundTicketPreReturnTempServiceImpl implements QuantFundTicketPreReturnTempService{
	
	@Autowired
	private QuantFundTicketPreReturnTempMapper quantFundTicketPreReturnTempMapper;
	
	@Override
	public QuantFundTicketPreReturnTemp getQuantFundTicketPreReturnTemp(Integer uid, String fundAssetCode) {
		return quantFundTicketPreReturnTempMapper.getQuantFundTicketPreReturnTemp(uid, fundAssetCode);
	}

}
