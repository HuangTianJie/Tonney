package com.gte.asset.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserBankDto {
	
	String channelName;
	
	String userName;
	
	String channelAccountNo;
	
	Date createDate;
	
	String ip;

}
