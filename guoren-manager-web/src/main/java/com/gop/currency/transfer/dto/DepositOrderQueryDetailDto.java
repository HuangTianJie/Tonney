package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.DepositCurrencyOrderUser;
import com.gop.domain.enums.DepositCurrencyOrderStatus;

import lombok.Data;
@Data
public class DepositOrderQueryDetailDto {

	//@ApiModelProperty("订单创建时间")
	Date createDate;
	
	//@ApiModelProperty("id")
	Integer id;
	
	//@ApiModelProperty("券商id")
	Integer brokerId;
	
	//@ApiModelProperty("用户id")
	Integer uid;
	
	//@ApiModelProperty("用户姓名")
	String name;

	//@ApiModelProperty("订单号")
	String orderNo;

	//@ApiModelProperty("订单号")
	String account;
	
	//@ApiModelProperty("申请金额")
	BigDecimal applyAmount;
	
	//@ApiModelProperty("实际金额")
	BigDecimal realAmount;
	
	//@ApiModelProperty("订单状态")
	DepositCurrencyOrderStatus status;

	//@ApiModelProperty("充值渠道")
	String channelType;

	//@ApiModelProperty("充值渠道账号")
	String channelAccountNo;
	
	public DepositOrderQueryDetailDto(DepositCurrencyOrderUser order){
		this.setCreateDate(order.getCreateDate());
		this.setApplyAmount(order.getMoney());
		this.setChannelAccountNo(order.getAcnumber());
		this.setChannelType(order.getBank());
		this.setId(order.getId());
		this.setAccount(order.getAccount());
		this.setName(order.getName());
		this.setOrderNo(order.getTxid());
		this.setRealAmount(order.getPay());
		this.setStatus(order.getStatus());
		this.setUid(order.getUid());
		
	}
}
