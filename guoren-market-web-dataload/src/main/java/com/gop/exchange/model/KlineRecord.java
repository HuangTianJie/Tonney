package com.gop.exchange.model;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
public class KlineRecord {
	@Indexed(unique = true)
	private Long time;

	private KlineType klineType;

	private String symbol;

	private BigDecimal open;

	private BigDecimal close;

	private BigDecimal maxPrice;

	private BigDecimal lowPrice;

	private BigDecimal amount;

	public enum KlineType {
		MIN1, MIN5, MIN15, MIN30, MIN60, DAY, MONTH, YEAR;
	}

}
