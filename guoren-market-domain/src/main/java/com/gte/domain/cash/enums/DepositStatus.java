package com.gte.domain.cash.enums;

/**
 * 审核状态
 */
public enum DepositStatus {

    /**
     * 待查账
     */
    WAITAUDIT("待查账"),

    /**
     * 待审核
     */
    CHECK("待审核"),

    /**
     * 审核通过
     */
    SUCCESS("审核通过"),

    /**
     * 充值失败
     */
    REFUSE("充值失败");

    //private String code;
    private String desc;

    private DepositStatus(String desc) {
        //this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
