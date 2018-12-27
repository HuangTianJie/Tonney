package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.DepositCurrencyOrderUser;

import lombok.Data;

@Data
public class DepositMatchQueryDetailDto {

	// @ApiModelProperty("订单号")
	private String serialNumber;

	// @ApiModelProperty("到账时间")
	private Date arriveTime;

	// @ApiModelProperty("到账金额")
	private BigDecimal money;

	// @ApiModelProperty("用户名字")
	private String name;

	// @ApiModelProperty("渠道名称")
	private String bankName;

	// @ApiModelProperty("账号")
	private String bankNo;

	// @ApiModelProperty("")
	private String postscript;

	// @ApiModelProperty("uid")
	private Integer uid;

	private Integer id;

	public DepositMatchQueryDetailDto(DepositCurrencyOrderUser depositCurrencyOrderUser) {
		this.uid = depositCurrencyOrderUser.getUid();
		this.arriveTime = depositCurrencyOrderUser.getCreateDate();
		this.bankName = depositCurrencyOrderUser.getThirdAccountName();
		this.bankNo = depositCurrencyOrderUser.getAcnumber();
		this.money = depositCurrencyOrderUser.getMoney();
		this.name = depositCurrencyOrderUser.getName();
		this.postscript = depositCurrencyOrderUser.getMsg();
		this.serialNumber = depositCurrencyOrderUser.getTxid();
		this.id = depositCurrencyOrderUser.getId();
	}
}
