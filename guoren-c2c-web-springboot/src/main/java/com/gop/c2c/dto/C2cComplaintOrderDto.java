package com.gop.c2c.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.domain.enums.C2cPayType;
import com.gop.domain.enums.C2cTransType;

import lombok.Data;

@Data
public class C2cComplaintOrderDto {
	@NotBlank
	private String transOrderId;
	@NotBlank
	private String complainReason;
	// 申诉人电话号码
	@NotBlank
	private String phone;
	private C2cPayType payType;
	private String payNo;
	private String capture;
	@NotBlank
	private String remark;
}
