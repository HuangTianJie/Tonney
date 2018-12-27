package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderDepositAudit;
import com.gte.domain.cash.enums.DepositResultStatus;
import com.gte.domain.cash.enums.DepositStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 普通用户法币充值操作记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "audit对象", description = "充值审核对象")
public class CashOrderDepositAuditDto {

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    private Integer id;

    /**
     * 订单号
     */
    @NonNull
    @ApiModelProperty(value="订单号")
    private String orderNo;

    /**
     * 处理前状态：待查账，待审核，审核通过，充值失败
     */
    @NonNull
    @ApiModelProperty(value="处理前状态：待查账，待审核，审核通过，充值失败")
    private DepositStatus beforeStatus;

    /**
     *
     */
    @NonNull
    @ApiModelProperty(value="处理后状态：待查账，待审核，审核通过，充值失败")
    private DepositStatus afterStatus;

    /**
     * 处理结果：通过，不通过，确认到账
     */
    @ApiModelProperty(hidden = true,value="处理结果：已确认到账，已审核，校验成功，驳回")
    private DepositResultStatus result;

    @JSONField(serialize = false)
    @ApiModelProperty(value = "到账金额")
    private BigDecimal realNumber;
    /**
     * 说明
     */
    @ApiModelProperty(value="说明")
    private String explain;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 修改时间
     */
    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;

    /**
     * 管理员帐号
     */
    @ApiModelProperty(hidden = true)
    private Integer adminId;

    @ApiModelProperty(hidden = true)
    private String mobile;
    /**
     * 管理员帐号名称
     */
    @ApiModelProperty(hidden = true)
    private String opName;

    @NonNull
    @JSONField(serialize = false)
    private Integer version;

    public CashOrderDepositAuditDto(CashOrderDepositAudit audit) {
        this.id = audit.getId();
        this.orderNo = audit.getOrderNo();
        this.beforeStatus = audit.getBeforeStatus();
        this.afterStatus = audit.getAfterStatus();
        this.result = audit.getResult();
        this.explain = audit.getExplain();
        this.createDate = audit.getCreateDate();
        this.adminId  = audit.getAdminId();
        this.mobile = audit.getMobile();
        this.updateDate = audit.getUpdateDate();
        this.opName = audit.getOpName();
    }
}