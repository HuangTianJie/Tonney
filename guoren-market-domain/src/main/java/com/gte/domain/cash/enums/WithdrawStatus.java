package com.gte.domain.cash.enums;

/**
 * 审核状态
 */
public enum WithdrawStatus {

    /**
     * 待审核-转账
     */
    WAITAUDIT("待审批-转账"),

    /**
     * 待确认-转账
     */
    CONFIRM("待确认-转账"),

    /**
     * 待审核-扣款
     */
    DEDUCT("待审核-扣款"),
    /**
     * 提现成功
     */
    SUCCESS("提现成功"),

    /**
     * 提现失败
     */
    REFUSE("提现失败");

    private String desc;

    private WithdrawStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
