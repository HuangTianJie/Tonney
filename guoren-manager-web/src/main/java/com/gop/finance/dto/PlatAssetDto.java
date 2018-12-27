package com.gop.finance.dto;

import com.gop.domain.BeginingBalance;
import com.gop.domain.Finance;
import com.gop.mapper.dto.FinanceAmountDto;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by wuyanjie on 2018/4/26.
 */
@Data
public class PlatAssetDto {

    private String assetCode;

    private BigDecimal amountAvailable;

    private BigDecimal amountLock;

    private BigDecimal amountTotal;

    public PlatAssetDto(FinanceAmountDto finance){
        this.assetCode = finance.getAssetCode();
        this.amountAvailable = finance.getAmountAvailable();
        this.amountLock = finance.getAmountLock();
        this.amountTotal = finance.getAmountAvailable().add(finance.getAmountLock());
    }

    public PlatAssetDto(BeginingBalance beginingBalance){
        this.assetCode = beginingBalance.getAssetCode();
        this.amountAvailable = beginingBalance.getAmount_available();
        this.amountLock = beginingBalance.getAmount_lock();
        this.amountTotal =beginingBalance.getAmount_total();
    }
}
