package com.gop.finance.dto;

import com.gop.domain.FinanceTotal;

import java.math.BigDecimal;

import lombok.Data;
import lombok.ToString;

/**
 * Created by YAO on 2018/4/26.
 */
@Data
public class FinanceTotalDto {
  private String assetCode;
  private BigDecimal inoutBalance;
  private BigDecimal walletBalance;
  private BigDecimal assetBalance;
  private BigDecimal depositWithdrawBalance;
  private BigDecimal walletDiff;
  private BigDecimal platWalletDiff;
  private BigDecimal platDiff;
  public FinanceTotalDto(FinanceTotal total){
    this.assetCode = total.getAssetCode();
    this.assetBalance = total.getAssetBalance();
    this.inoutBalance = total.getInoutBalance();
    this.walletBalance = total.getWalletBalance();
    this.depositWithdrawBalance = total.getDepositWithdrawBalance();
    this.walletDiff = total.getWalletDiff();
    this.platWalletDiff = total.getPlatWalletDiff();
    this.platDiff = total.getPlatDiff();
  }
}
