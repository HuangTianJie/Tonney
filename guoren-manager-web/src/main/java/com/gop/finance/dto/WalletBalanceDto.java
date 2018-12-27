package com.gop.finance.dto;

import com.gop.domain.WalletBalance;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Created by YAO on 2018/4/25.
 */
@Data
public class WalletBalanceDto {
  private Integer id;
  private String assetCode; // 币种
  private String account; // 账户地址
  private String walletType; // 冷热钱包
  private BigDecimal balance; // 余额
  public WalletBalanceDto(WalletBalance balance){
    this.id = balance.getId();
    this.assetCode = balance.getAssetCode();
    this.account = balance.getAccount();
    this.balance = balance.getBalance();
    this.walletType = balance.getWalletType();
  }
}
