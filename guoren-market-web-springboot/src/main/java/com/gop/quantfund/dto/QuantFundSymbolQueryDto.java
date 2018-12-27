package com.gop.quantfund.dto;


import com.gop.domain.enums.QuantFundConfigSymbolStatus;

import lombok.Data;
@Data
public class QuantFundSymbolQueryDto {
	// 基金交易对状态
	private QuantFundConfigSymbolStatus symbolStatus;
	// 入场券状态
	private String ticketStatus;
}
