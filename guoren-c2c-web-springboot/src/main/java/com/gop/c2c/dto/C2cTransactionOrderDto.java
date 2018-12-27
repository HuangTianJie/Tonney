package com.gop.c2c.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class C2cTransactionOrderDto {
	private String c2cTransOrderNum;
	private List<C2cBasePayChannelDto> dtoList;
	private Date currentTime;
	private Date orderCreateDate;
}
