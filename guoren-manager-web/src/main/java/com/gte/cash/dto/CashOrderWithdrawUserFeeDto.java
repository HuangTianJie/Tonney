package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;
import io.swagger.annotations.ApiModelProperty;
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
public class CashOrderWithdrawUserFeeDto {

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
    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    public CashOrderWithdrawUserFeeDto(CashOrderWithdrawUserFee o) {
        this.assetCode = o.getAssetCode();
        this.name = o.getName();
        this.txFee = o.getTxFee();
        this.createDate = o.getCreateDate();
    }
}