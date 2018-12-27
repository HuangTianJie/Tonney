package com.gte.domain.agent;

import com.gte.domain.enums.ChrgStdType;
import com.gte.domain.enums.SetMthd;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 费率配置表
 */
@Data
public class FundFeeRateCfg {

    /**
     * 费率ID
     */
    private Long feeRateCfgId;

    /**
     * 税费规则ID
     */
    private Long feeRuleCfgId;

    /**
     * 税率名称
     */
    private String rateNam;

    /**
     * 结算方式
     */
    private SetMthd setMthd;

    /**
     * 收费标准类型
     */
    private ChrgStdType chrgStdType;

    /**
     * 收费标准
     */
    private String chrgStd;

    /**
     * 流量开始区间
     */
    private BigDecimal sttIntFlw;

    /**
     * 流量截止区间
     */
    private BigDecimal endIntFlw;

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
     * 创建人员
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