package com.gop.currency.transfer.dto;


import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.domain.enums.DepositCurrencyPayMode;
import com.gop.domain.enums.SwitchStatus;

import lombok.Data;

@Data
public class ChannelRulesDto {
	
//	@ApiModelProperty("id,如果新增支付规则，改值为空")
	Integer id;

//	@ApiModelProperty("渠道。目前只支持OKPAY（海外）或者ALIPAY（国内）")
	@NotNull
	DepositCurrencyPayMode channelType;
	
//	@ApiModelProperty("备注")
	String remark;
	
//	@ApiModelProperty("二维码，目前只有支付宝有二维码")
	@NotBlank
	String code;
	
//	@ApiModelProperty("收款账号")
	@NotBlank
	String account;
	
//	@ApiModelProperty("收款账号姓名")
	@NotNull
	String name;
	
//	@ApiModelProperty("支付规则开启状态")
	@NotNull
	SwitchStatus status;
	
//	@ApiModelProperty("用户id付款规则")
	@NotNull
	String rules;


}
