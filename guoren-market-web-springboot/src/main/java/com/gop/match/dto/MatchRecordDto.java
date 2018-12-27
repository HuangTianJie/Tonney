package com.gop.match.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.TradeMatchResult;

import lombok.Data;

@Data
public class MatchRecordDto {

	private BigDecimal price;

	private BigDecimal num;

	private Date createTime;

	public MatchRecordDto(TradeMatchResult tradeMatchResult) {
		this.price = tradeMatchResult.getPrice();
		this.num = tradeMatchResult.getNumber();
		this.createTime = tradeMatchResult.getCreateTime();

	}

}
