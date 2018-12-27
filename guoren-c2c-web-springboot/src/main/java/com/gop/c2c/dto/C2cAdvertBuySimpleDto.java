
package com.gop.c2c.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.mode.vo.BaseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2cAdvertBuySimpleDto extends BaseDto{
	
	//昵称
	private String nickName; 
	//国家
	private String country ;
	//交易币种
	private String assetCode;
	//货币
	private String currency;
	//交易价格
	private BigDecimal tradePrice; 
	//预计购买
	private BigDecimal buyPrice; 
	//备注信息
	private String remark;
	//交易次数
	private Integer tradeCount;
	//被鼓励次数
	private Integer encourageCount;
	//手机号
	private String phone;
	//广告ID
	private String advertId;
	//创建时间
	private Date createDate;
	//用户交易次数、鼓励次数及交易信息
	private C2cTransactionInfoDto c2cTransactionInfoDto;
	
	 
}
