package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.domain.enums.SwitchStatus;

import lombok.Data;

@Data
public class ChannelConfigDto {
	
	//@ApiModelProperty("渠道名：目前国内是ALIPAY,国外是OKPAY")
	@NotBlank
	String channelType;
	
	//@ApiModelProperty("配置的id,新增配置的时候为不传值")
	Integer id;

	//@ApiModelProperty("最小充值金额")
	@NotNull
	BigDecimal minAmount;
	
	//@ApiModelProperty("最大充值金额")
	@NotNull
	BigDecimal maxAmount;
	
	//@ApiModelProperty("配置状态")
	@NotNull
	SwitchStatus status;
	
	//@ApiModelProperty("配置状态")
	Date createDate;

}
