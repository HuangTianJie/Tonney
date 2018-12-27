package com.gop.coin.transfer.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.mode.vo.BaseDto;

import lombok.Data;

@Data
public class TransferInAddressDto extends BaseDto{

	@NotBlank
	private String assetCode;
	
	@NotNull
	private Integer addressid;
	
	@NotBlank
	private String address;
	
	public TransferInAddressDto(ChannelCoinAddressDeposit address){
		this.assetCode = address.getAssetCode();
		this.addressid = address.getId();
		this.address = address.getCoinAddress();
	}

}
