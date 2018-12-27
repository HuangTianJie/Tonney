package com.gte.domain.cash.dto;

import com.gte.domain.cash.enums.DepositResultStatus;
import com.gte.domain.cash.enums.DepositStatus;
import lombok.*;

import java.util.Date;

/**
 * 普通用户法币充值操作记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOrderDepositAudit {

    /**
     * id
     */
    private Integer id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 处理前状态：待查账，待审核，审核通过，充值失败
     */
    private DepositStatus beforeStatus;

    /**
     * 处理后状态：待查账，待审核，审核通过，充值失败
     */
    private DepositStatus afterStatus;

    /**
     * 处理结果：通过，不通过，确认到账
     */
    private DepositResultStatus result;

    /**
     * 说明
     */
    private String explain;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date updateDate;

    /**
     * 管理员帐号
     */
    private Integer adminId;
    /**
     * 管理员帐号名称
     */
    private String opName;

    private String mobile;

}