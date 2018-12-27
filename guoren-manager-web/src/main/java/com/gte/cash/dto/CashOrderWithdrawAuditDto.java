package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.domain.cash.enums.WithdrawResultStatus;
import com.gte.domain.cash.enums.WithdrawStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 普通用户法币提现操作记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "WithdrawDto对象", description = "充值订单审核对象")
public class CashOrderWithdrawAuditDto {

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    private Integer id;

    /**
     * 订单id
     */
    @NonNull
    @ApiModelProperty(value="订单号")
    private String orderNo;

    /**
     * 处理前状态：待审核-转账，待确认转账，待审核-扣款，提现完成，提现失败
     */
    @NonNull
    @ApiModelProperty(value="处理前状态：待审核-转账，待确认-转账，待审核-扣款，提现完成，提现失败",example="WAITAUDIT")
    private WithdrawStatus beforeStatus;

    /**
     * 处理后状态：待审核-转账，待确认转账，待审核-扣款，提现完成，提现失败
     */
    @NonNull
    @ApiModelProperty(value="处理前状态：待审核-转账，待确认-转账，待审核-扣款，提现完成，提现失败",example="CONFIRM")
    private WithdrawStatus afterStatus;

    /**
     * 处理结果：通过，不通过，确认转账，批准
     */
    @ApiModelProperty(hidden = true,value="处理结果：批准，确认转账，审核成功，驳回",example="CONFIRM")
    private WithdrawResultStatus result;

    /**
     * 说明
     */
    @ApiModelProperty(value="说明",example="asdfa")
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

    @JSONField(serialize = false)
    private List<CashOrderWithdrawUserFeeDto> feeList;

    public CashOrderWithdrawAuditDto(CashOrderWithdrawAudit audit) {
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