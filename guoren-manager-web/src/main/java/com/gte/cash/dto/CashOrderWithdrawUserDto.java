package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.enums.WithdrawStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOrderWithdrawUserDto {
    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer uid;

    @ApiModelProperty(hidden = true)
    private String fullName;
    /**
     * 券商id
     */
    private Integer brokerId;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 户名
     */
    private String cardName;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 开户行swift code
     */
    private String swiftCode;

    /**
     * 资产类型
     */
    private String assetCode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 总数：实际数+手续费
     */
    private BigDecimal number;

    /**
     * 实际数量
     */
    private BigDecimal realNumber;

    /**
     * 消息
     */
    private String msg;

    /**
     * 订单状态：待审核-转账，待确认-转账，待审核-扣款，提现完成，提现失败
     */
    private WithdrawStatus status;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;

    /**
     * tx手续费
     */
    private BigDecimal txFee;

    /**
     * 管理员帐号
     */
    private Integer adminId;

    /**
     * 是否关注
     */
    private Integer watchful;

    private Integer version;

    private List<CashOrderWithdrawAudit> auditDtoList;

    private List<CashOrderWithdrawUserFeeDto> feeList;

    public CashOrderWithdrawUserDto(CashOrderWithdrawUser order) {
        if(order == null)
            return;
        this.id = order.getId();
        this.uid = order.getUid();
        this.brokerId = order.getBrokerId();
        this.accountId = order.getAccountId();
        this.account = order.getAccount();
        this.assetCode = order.getAssetCode();
        this.orderNo = order.getOrderNo();
        this.number = order.getNumber();
        this.realNumber = order.getRealNumber();
        this.txFee = order.getTxFee();
        this.cardName = order.getCardName();
        this.cardNumber = order.getCardNumber();
        this.bankName = order.getBankName();
        this.swiftCode = order.getSwiftCode();
        this.msg = order.getMsg();
        this.status = order.getStatus();
        this.createDate = order.getCreateDate();
        this.updateDate = order.getUpdateDate();
        this.watchful = order.getWatchful();
        this.version = order.getVersion();
    }
}