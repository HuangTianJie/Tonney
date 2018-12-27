package com.gte.user.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ConfigSymbolProfileInitDto extends ConfigSymbolDto{
	@NotNull
	private Integer pricePrecision;// ---价格精度 8
	@NotNull
	private Integer amountPrecision;// ---数量精度 4
	@NotNull
	private Integer highlightNo; // ---高亮位数 5
}
