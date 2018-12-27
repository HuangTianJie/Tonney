package com.gop.currency.transfer.dto;

import lombok.Data;
@Data
public class DepositMatchDto {
	
	//@ApiModelProperty("订单id")
	Integer orderId;
	
	//@ApiModelProperty("银行订单id")
	Integer bankId;
}
