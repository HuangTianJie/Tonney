package com.gop.finance.dto;

import com.gop.domain.WalletInOutTotal;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Created by YAO on 2018/4/26.
 */
@Data
public class WalletInOutTotalDto {
  private String assetCode;
  private BigDecimal depositAmount;
  private BigDecimal withDrawAmount;
  private BigDecimal preAmount;
  private BigDecimal totalAmount;

  public WalletInOutTotalDto(WalletInOutTotal total){
    this.assetCode = total.getAssetCode();
    this.depositAmount = total.getDepositAmount();
    this.withDrawAmount = total.getWithDrawAmount();
    this.preAmount = total.getPreAmount();
    this.totalAmount = total.getTotalAmount();
  }
}
