package com.gop.quantfund;

import com.gop.domain.QuantFundAdmissionTicket;
import com.gop.domain.enums.QuantFundAdmissionTicketStatus;

public interface QuantFundAdmissionTicketService {
	
	public QuantFundAdmissionTicket getQuantFundAdmissionTicketByUidAndFundAssetCode(Integer uid,String fundAssetCode);
	
	public void buyQuantFundAdmissionTicket(Integer uid,String fundAssetCode);
	
	public void addQuantFundAdmissionTicket(QuantFundAdmissionTicket quantFundAdmissionTicket);
	
	public void returnQuantFundAdmissionTicket(Integer uid,String fundAssetCode);
	
	public int updateQuantFundAdmissionTicketStatusById(Integer id,QuantFundAdmissionTicketStatus beginStatus,QuantFundAdmissionTicketStatus endStatus);
	
	public void preReturnQuantFundAdmissionTicket(Integer uid,String fundAssetCode);
}
