package com.gop.match.dto;

import com.gop.domain.enums.TradeCoinType;
import com.gte.domain.sector.enums.Sector;
import lombok.*;

import java.math.BigDecimal;

/**
 * 手续费
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeDto {
    BigDecimal fee = BigDecimal.ZERO;
    String assetCode;
    /**
     * 判断是否在创新中，手续费收交易币
     */
    Boolean isCollect = false ;
    BigDecimal buyFeeRate;
}
