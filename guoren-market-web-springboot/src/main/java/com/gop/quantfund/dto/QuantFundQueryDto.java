package com.gop.quantfund.dto;

import java.math.BigDecimal;

import com.gop.domain.enums.AuthLevel;

import lombok.Data;

@Data
public class QuantFundQueryDto {
		// 基金开始日
		private Long fundBeginDate;
		// 基金赎回日
		private Long fundEndDate;
		// 入场券开关
		private String ticketStatus;
}
