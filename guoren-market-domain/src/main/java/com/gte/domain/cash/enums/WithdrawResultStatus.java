package com.gte.domain.cash.enums;

/**
 * 审核状态
 */
public enum WithdrawResultStatus {

    /**
     * 批准
     */
    AUTHORIZE,
    /**
     * 确认转账
     */
    CONFIRM,
    /**
     * 审核成功
     */
    SUCCESS,

    /**
     * 驳回
     */
    REFUSAL,
    /**
     * 审核失败
     */
    REFUSE;
}
