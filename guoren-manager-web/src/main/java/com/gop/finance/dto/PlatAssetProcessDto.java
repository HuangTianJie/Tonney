package com.gop.finance.dto;

import com.gop.domain.PlatAssetProcess;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by wuyanjie on 2018/4/27.
 */
@Data
public class PlatAssetProcessDto {
    private String symbol;
    private BigDecimal beginBalance; //期初余额
    private BigDecimal depositBalance;  //存入金额
    private BigDecimal withdraw;  //提现-申请（未确认+成功+失败）
    private BigDecimal withdrawUnknow;  //提现-未确认
    private BigDecimal withdrawSuccess;  //提现-成功
    private BigDecimal withdrawRefuse;   //提现-失败
    private BigDecimal withdrawFee;      //提现-手续费
    private BigDecimal brokenAsset;      //管理员调整资产
    private BigDecimal tradeFee;        //交易手续费
    private BigDecimal other;          //其他
    private BigDecimal endBalance;    //期末余额
    private BigDecimal culBalance; //计算余额  存入金额+存入金额-提现-成功-提现-手续费+管理员调整资产-交易手续费+其他

    public PlatAssetProcessDto(){}
    public PlatAssetProcessDto(PlatAssetProcess process){
        this.symbol = process.getAssetCode();
        this.beginBalance = process.getBeginBalance();
        this.depositBalance = process.getDepositBalance();
        this.withdraw = process.getWithdrawTotal();
        this.withdrawUnknow = process.getWithdrawUnknow();
        this.withdrawSuccess = process.getWithdrawSuccess();
        this.withdrawRefuse = process.getWithdrawRefuse();
        this.withdrawFee = process.getWithdrawFee();
        this.brokenAsset = process.getBrokenAssetBalance();
        this.tradeFee = process.getTradeFee();
        this.other = process.getOther();
        this.endBalance = process.getEndBalance();
        this.culBalance = process.getCulBalance();
    }
}