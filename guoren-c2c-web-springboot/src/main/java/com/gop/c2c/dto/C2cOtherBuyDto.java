package com.gop.c2c.dto;

import java.math.BigDecimal;


import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class C2cOtherBuyDto {
private BigDecimal tradePrice; 
private String assetCode;
private String advertId;
}
