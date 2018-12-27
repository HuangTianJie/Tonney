package com.gop.c2c.dto;

import java.math.BigDecimal;
import java.util.List;

import com.gop.domain.enums.C2cPayType;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class C2cOtherSellDto {
private BigDecimal tradePrice;
private List<C2cPayType> c2cPayType;
private String advertId;
}
