package com.gop.finance.dto;

import com.gop.domain.WalletBalanceTotal;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Created by YAO on 2018/4/26.
 */
@Data
public class WalletBalacneTotalDto {
  private String assetCode;
  private BigDecimal hotBalance;
  private BigDecimal coldBalance;
  private BigDecimal totalBalance;

  public WalletBalacneTotalDto(WalletBalanceTotal total){
    this.assetCode = total.getAssetCode();
    this.hotBalance = total.getHotBalance();
    this.coldBalance = total.getColdBalance();
    this.totalBalance = total.getTotalBalance();
  }
}
