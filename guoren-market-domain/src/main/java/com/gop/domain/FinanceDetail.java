package com.gop.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户帐单信息，用来前端显示用户资产变化：普通每笔订单撮合完后生成记录（完全成交和撤单），所有多资金变化
 */
@Data
public class FinanceDetail {
    private Integer id;

    private Integer uid;

    /**
     * currency资产类型
     */
    private String assetCode;

    private String requestNo;

    /**
     * 业务类型，决定out_tx_no来自那一张表
     */
    private String businessSubject;

    /**
     * 0:总资产未变化1:总资产发生变化
     */
    private Integer assetChangeType;

//    private Integer subject1;
//
//    private Integer subject2;

    /**
     * 可用变化：正数表示增加，负数表示减少
     */
    private BigDecimal amountAvailable;

    /**
     * 冻结变化：正数表示增加，负数表示减少
     */
    private BigDecimal amountLock;

    /**
     * 借贷变化：正数表示增加，负数表示减少
     */
    private BigDecimal amountLoan;

    /**
     * 变化前可用金额
     */
    private BigDecimal balanceOldAvailable;

    /**
     * 变化前冻结金额
     */
    private BigDecimal balanceOldLock;

    /**
     * 变化前借贷金额
     */
    private BigDecimal balanceOldLoan;

    /**
     * 变化后可用金额
     */
    private BigDecimal balanceNewAvailable;

    /**
     * 变化后冻结金额
     */
    private BigDecimal balanceNewLock;

    /**
     * 变化后借贷金额
     */
    private BigDecimal balanceNewLoan;

    private Date createDate;

}