package com.gte.domain.agent;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 税费规则配置表
 */
@Data
public class FundFeeRuleCfg {

    /**
     * 税费规则配置ID
     */
    private Long feeRuleCfgId;

    /**
     * 规则名称
     */
    private String ruleNam;

    /**
     * 费用类型
     */
    private String feeType;

    /**
     * 费用明细
     */
    private String feeItem;

    /**
     * 交易来源，区分业务
     */
    private String exchgCd;

    /**
     * 操作代码
     */
    private String opCd;

    /**
     * 应付方角色代码
     */
    private String apRoleCd;

    /**
     * 应收方角色代码
     */
    private String arRoleCd;

    /**
     * 有效开始时间
     */
    private Date validSttTime;

    /**
     * 有效截止时间
     */
    private Date validEndTime;

    /**
     * 费用上限
     */
    private BigDecimal feeMaxLimit;

    /**
     * 费用下限
     */
    private BigDecimal feeMinLimit;

    /**
     * 收费方式
     */
    private String chrgMthd;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 小数处理规则
     */
    private String decProcRule;

    /**
     * 规则处理类型
     */
    private String ruleProcType;

    /**
     * 状态
     */
    private String stt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标识
     */
    private String delFlg;

    /**
     * 版本号
     */
    private Long verId;

    /**
     * 创建人
     */
    private Long creUsr;

    /**
     * 创建时间
     */
    private Date creTime;

    /**
     * 更新人
     */
    private Long updUsr;

    /**
     * 更新时间
     */
    private Date updTime;

}