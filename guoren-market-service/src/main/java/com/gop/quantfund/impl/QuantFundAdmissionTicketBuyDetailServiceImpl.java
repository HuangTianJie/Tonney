package com.gop.quantfund.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.QuantFundAdmissionTicketBuyDetail;
import com.gop.mapper.QuantFundAdmissionTicketBuyDetailMapper;
import com.gop.quantfund.QuantFundAdmissionTicketBuyDetailService;


@Service
public class QuantFundAdmissionTicketBuyDetailServiceImpl implements QuantFundAdmissionTicketBuyDetailService {

	@Autowired
	private QuantFundAdmissionTicketBuyDetailMapper quantFundAdmissionTicketBuyDetailMapper;
	
	@Override
	public void addQuantFundAdmissionTicketBuyDetail(QuantFundAdmissionTicketBuyDetail quantFundAdmissionTicketBuyDetail) {
		quantFundAdmissionTicketBuyDetailMapper.addQuantFundAdmissionTicketBuyDetail(quantFundAdmissionTicketBuyDetail);
	}

}
