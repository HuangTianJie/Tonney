package com.gop.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by wuyanjie on 2018/5/23.
 */
@Data
public class AssetCoinDto {
    private String assetCode;
    private BigDecimal depositNumber;  //充值
    private BigDecimal withdrawNumber;  //提现
    private BigDecimal netNumberl;    //净额

    public AssetCoinDto(String assetCode,BigDecimal depositNumber,BigDecimal withdrawNumber){
        this.assetCode = assetCode;
        this.depositNumber = depositNumber.setScale(8,BigDecimal.ROUND_HALF_UP);
        this.withdrawNumber = withdrawNumber.setScale(8,BigDecimal.ROUND_HALF_UP);
        this.netNumberl = depositNumber.subtract(withdrawNumber).setScale(8,BigDecimal.ROUND_HALF_UP);
    }
}
