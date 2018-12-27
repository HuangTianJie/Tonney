package com.gop.currency.transfer.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;

import lombok.Data;

@Data
public class WithdrawOrderQueryDetailDto {

	// @ApiModelProperty("订单创建时间")
	Date createDate;
	
	Date updateDate;

	// @ApiModelProperty("id")
	Integer id;

	// @ApiModelProperty("用户id")
	Integer uid;

	// @ApiModelProperty("券商id")
	Integer brokerId;

	// @ApiModelProperty("用户姓名")
	String name;

	// @ApiModelProperty("订单号")
	String orderNo;

	// @ApiModelProperty("申请金额")
	BigDecimal applyAmount;

	// @ApiModelProperty("实际金额")
	BigDecimal realAmount;
	
	BigDecimal fee;
	
	String msg;
	
	// @ApiModelProperty("订单状态")
	WithdrawCurrencyOrderStatus status;

	String payMode;

	// @ApiModelProperty("提现渠道")
	String channelType;

	// @ApiModelProperty("提现渠道账号")
	String channelAccountNo;

	public WithdrawOrderQueryDetailDto(WithdrawCurrencyOrderUser order){
		this.setMsg(order.getMsg());
		this.setCreateDate(order.getCreateDate());
		this.setUpdateDate(order.getUpdateDate());
		this.setApplyAmount(order.getMoney());
		this.setChannelAccountNo(order.getAcnumber());
		this.setChannelType(order.getBank());
		this.setId(order.getId());
		this.setName(order.getName());
		this.setOrderNo(order.getInnerOrderNo());
		this.setRealAmount(order.getPay());
		this.setFee(order.getFee());
		this.setStatus(order.getStatus());
		this.setUid(order.getUid());
		this.setPayMode(order.getPayMode()==null?null:order.getPayMode().toString());
		
	}
}
