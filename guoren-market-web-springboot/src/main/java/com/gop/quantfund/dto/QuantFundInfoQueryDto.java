package com.gop.quantfund.dto;

import java.math.BigDecimal;

import com.gop.domain.enums.AuthLevel;

import lombok.Data;
@Data
public class QuantFundInfoQueryDto {
	// 账号(邮箱)
	private String userAccount;
	// 币种
	private String fundAssetCode;
	// 币种数量
	private BigDecimal fundAssetCodeAmount;
	// 基金净值
//	private BigDecimal worth;
	// 基金总净值
//	private BigDecimal totalWorth;
//	// 基金当前市场价,单位USDT
//	private BigDecimal perUnit;
	// 用户认证信息
	private AuthLevel level;
	// 入场券是否购买
	private String buyTicket;
	// 基金开始日
	private Long fundBeginDate;
	// 基金赎回日
	private Long fundEndDate;
	// 入场券开关
	private String ticketStatus;
	//是否可以提前兑回
	private boolean isPreReturn;
}
