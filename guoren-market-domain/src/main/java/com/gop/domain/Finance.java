package com.gop.domain;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资产表
 */
@Data
@ToString
public class Finance {
    private Integer id;

    private Integer uid;
    
    private Integer brokerId;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 账户类型：主-MASTER，子帐户-SUB，借贷帐户-LOAN等
     */
    private String accountKind;

    /**
     * 资产代码
     */
    private String assetCode;

    /**
     * 可用金额
     */
    private BigDecimal amountAvailable;

    /**
     * 冻结金额
     */
    private BigDecimal amountLock;

    /**
     * 借贷金额
     */
    private BigDecimal amountLoan;

    private Date updateDate;

    private Integer version;

}