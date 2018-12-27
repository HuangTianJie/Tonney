package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.DepositMatchBankOrderUser;
import com.gop.domain.DepositMatchCurrencyOrderUser;
import com.gop.domain.enums.MatchState;

import lombok.Data;

@Data
public class DepositMatchOrderDetailDto {

	// @ApiModelProperty("状态/操作")
	MatchState status;

	// @ApiModelProperty("匹配来源")
	String source;

	// @ApiModelProperty("姓名")
	String name;

	// @ApiModelProperty("汇款账号")
	String account;

	// @ApiModelProperty("金额")
	BigDecimal amount;

	// @ApiModelProperty("到账时间")
	Date date;

	// @ApiModelProperty("备注/附言")
	String msg;

	// @ApiModelProperty("操作日志")
	String logs;

	public DepositMatchOrderDetailDto(DepositMatchCurrencyOrderUser depositMatchCurrencyOrderUser) {
             
	}

}
