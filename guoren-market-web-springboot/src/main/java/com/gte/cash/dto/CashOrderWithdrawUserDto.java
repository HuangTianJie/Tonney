package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.enums.WithdrawStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOrderWithdrawUserDto {

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private Integer id;

    /**
     * 用户id
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private Integer uid;

    /**
     * 券商id
     */
    @ApiModelProperty(example="10003")
    private Integer brokerId;

    /**
     * 账户id
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private Integer accountId;

    /**
     * 用户账号
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private String account;

    /**
     * 户名
     */
    @ApiModelProperty(value = "户名",example="king")
    private String cardName;

    /**
     * 卡号
     */
    @ApiModelProperty(value = "卡号",example="123123123123123a41231")
    private String cardNumber;

    /**
     * 开户行
     */
    @ApiModelProperty(value = "开户行",example="中央银行")
    private String bankName;

    /**
     * 开户行swift code
     */
    @ApiModelProperty(value = "开户行swift code", example="asdfasdfas23413412")
    private String swiftCode;

    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型", example="BTC")
    private String assetCode;

    /**
     * 订单号
     */
    @ApiModelProperty(hidden = true)
    private String orderNo;

    /**
     * 总数：实际数+手续费
     */
    @ApiModelProperty(value = "总数：实际数+手续费", example="11")
    private BigDecimal number;

    /**
     * 实际数量
     */
    @ApiModelProperty(value = "实际数量", example="10")
    private BigDecimal realNumber;

    /**
     * 消息
     */
    @ApiModelProperty(value = "消息", example="asdfa")
    private String msg;

    /**
     * 订单状态：待审核-转账，待确认-转账，待审核-扣款，提现完成，提现失败
     */
    @ApiModelProperty(hidden = true)
    private WithdrawStatus status;

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
    @JSONField(serialize = false)
    private Date updateDate;

    /**
     * tx手续费
     */
    @ApiModelProperty(value = "手续费", example="1")
    private BigDecimal txFee;

    /**
     * 管理员帐号
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private Integer adminId;

    public CashOrderWithdrawUserDto(CashOrderWithdrawUser order) {
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
        if(WithdrawStatus.CONFIRM.equals(order.getStatus())){
            this.status = WithdrawStatus.WAITAUDIT;
        }else if(WithdrawStatus.DEDUCT.equals(order.getStatus())){
            this.status = WithdrawStatus.WAITAUDIT;
        }else{
            this.status = order.getStatus();
        }
        this.createDate = order.getCreateDate();
    }
}