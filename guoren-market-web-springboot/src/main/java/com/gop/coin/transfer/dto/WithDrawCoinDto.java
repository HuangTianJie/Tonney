package com.gop.coin.transfer.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.mode.vo.BaseDto;

import lombok.Data;

@Data
public class WithDrawCoinDto extends BaseDto{

	@NotNull
	private BigDecimal amount;
	
	private BigDecimal fee;

	@NotNull
	private String outOrder;
	
	
	private Integer addressId;

	private String address;
	
	@NotBlank
	private String assetCode;

//	@NotBlank
	private String message;
	
	private String mobileid;
	
	private String code;

}
