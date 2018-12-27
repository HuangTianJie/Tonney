package com.gte.domain.cash.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 普通用户法币提现订单手续费
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOrderWithdrawUserFee {
    /**
     * id
     */
    private Integer id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 资产类型
     */
    private String assetCode;

    /**
     * 手续费名称
     */
    private String name;

    /**
     * tx手续费
     */
    private BigDecimal txFee;

    /**
     * 创建时间
     */
    private Date createDate;

}