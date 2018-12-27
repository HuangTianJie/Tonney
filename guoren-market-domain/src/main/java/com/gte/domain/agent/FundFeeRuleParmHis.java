package com.gte.domain.agent;

import lombok.Data;

import java.util.Date;

/**
 * 税费规则参数历史表
 */
@Data
public class FundFeeRuleParmHis {

    /**
     * 税率规则参数历史ID
     */
    private Long feeRuleParmHisId;

    /**
     * 税费规则参数ID
     */
    private Long feeRuleParmId;

    /**
     * 税费规则ID
     */
    private Long feeRuleCfgId;

    /**
     * 参数描述
     */
    private String parmDesc;

    /**
     * 参数KEY
     */
    private String parmKey;

    /**
     * 参数VALUE
     */
    private String parmVal;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记
     */
    private String delFlg;

    /**
     * 版本号
     */
    private Long verId;

    /**
     * 创建时间
     */
    private Date creTime;

    /**
     * 创建人
     */
    private Long creUsr;

    /**
     * 更新时间
     */
    private Date updTime;

    /**
     * 更新人员
     */
    private Long updUsr;

}