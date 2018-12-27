package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.DepositMatchBankOrderUser;
import com.gop.domain.enums.RechargeStatus;

import lombok.Data;

@Data
public class DepositBankOrderDetailDto {

	// @ApiModelProperty("状态/操作")
	RechargeStatus status;

	Integer id;
	// @ApiModelProperty("信息来源")
	String source;

	// @ApiModelProperty("流水号")
	String txid;

	// @ApiModelProperty("姓名")
	String name;

	// @ApiModelProperty("汇款账号")
	String account;

	// @ApiModelProperty("金额")
	BigDecimal amount;

	// @ApiModelProperty("时间")
	Date date;

	// @ApiModelProperty("备注/UID")
	String msg;

	public DepositBankOrderDetailDto(DepositMatchBankOrderUser depositMatchBankOrderUser) {
		this.account = depositMatchBankOrderUser.getAccountNo();
		this.amount = depositMatchBankOrderUser.getMoney();
		this.date = depositMatchBankOrderUser.getInsertDate();
		this.msg = depositMatchBankOrderUser.getPostscript();
		this.name = depositMatchBankOrderUser.getName();
		this.source = depositMatchBankOrderUser.getSource();
		this.status = depositMatchBankOrderUser.getStatus();
		this.txid = depositMatchBankOrderUser.getSerialNumber();
		this.id = depositMatchBankOrderUser.getId();
	}

}
