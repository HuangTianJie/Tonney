package com.gop.quantfund.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.QuantFundAdmissionTicketReturnDetail;
import com.gop.mapper.QuantFundAdmissionTicketReturnDetailMapper;
import com.gop.quantfund.QuantFundAdmissionTicketReturnDetailService;


@Service
public class QuantFundAdmissionTicketReturnDetailServiceImpl implements QuantFundAdmissionTicketReturnDetailService {

	@Autowired
	private QuantFundAdmissionTicketReturnDetailMapper quantFundAdmissionTicketReturnDetailMapper;
	
	@Override
	public void addQuantFundAdmissionTicketReturnDetail(QuantFundAdmissionTicketReturnDetail quantFundAdmissionTicketReturnDetail) {
		quantFundAdmissionTicketReturnDetailMapper.addQuantFundAdmissionTicketReturnDetail(quantFundAdmissionTicketReturnDetail);
	}

}
