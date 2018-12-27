package com.gop.finance.dto;

import com.gop.domain.WalletInOut;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * Created by YAO on 2018/4/24.
 */
@Data
public class WalletInOutDto {
  private String opt;
  private String assetCode;
  private String account; // address
  private BigDecimal amount;
  private String status;
  private Date updateTime;
  public WalletInOutDto(WalletInOut balance){
    this.opt = balance.getOpt();
    this.assetCode = balance.getAssetCode();
    this.account = balance.getOpt().equalsIgnoreCase("IN")?balance.getToAccount():balance.getFromAccount();
    this.amount = balance.getAmount();
    this.status = balance.getStatus();
    this.updateTime = balance.getUpdateTime();
  }
}
