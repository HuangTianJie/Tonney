package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashOrderDepositUser;
import com.gte.domain.cash.enums.DepositStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "deposit对象", description = "充值订单对象")
public class CashOrderDepositUserDto {

    @ApiModelProperty(hidden = true)
    private Integer id;

    @ApiModelProperty(hidden = true)
    private Integer uid;

    @ApiModelProperty(hidden = true)
    private String fullName;

    @ApiModelProperty(example="10003")
    @NotNull
    private Integer brokerId;

    @ApiModelProperty(hidden = true)
    private Integer accountId;

    @ApiModelProperty(hidden = true)
    private String account;
    /**
     * 户名
     */
    @ApiModelProperty(value = "户名",example="tom")
    private String cardName;

    /**
     * 卡号
     */
    @ApiModelProperty(value = "卡号",example="123123123123")
    private String cardNumber;
    /**
     * 资产类型
     */
    @NotNull
    @ApiModelProperty(value = "资产类型",example="BTC")
    private String assetCode;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号", hidden = true)
    private String orderNo;
    /**
     * 识别码
     */
    @ApiModelProperty(value="识别码")
    private String codeNo;

    /**
     * 凭证
     */
    @ApiModelProperty(value="凭证")
    private String fileName;

    /**
     * 汇款时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date time;

    @NotNull
    @ApiModelProperty(example="11")
    private BigDecimal number;

    @ApiModelProperty(hidden = true)
    private BigDecimal realNumber;

    @ApiModelProperty(example="init")
    private String msg;

    @ApiModelProperty(value="订单状态：待查账，待审核，审核通过，充值失败", hidden = true)
    private DepositStatus status;

    @ApiModelProperty(hidden = true)
    private BigDecimal fee;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;
    /**
     * 是否关注
     */
    @ApiModelProperty(hidden = true)
    private Integer watchful;

    private Integer version;

    private List<CashOrderDepositAuditDto> auditDtoList;

    public CashOrderDepositUserDto(CashOrderDepositUser order) {
        this.id = order.getId();
        this.uid = order.getUid();
        this.brokerId = order.getBrokerId();
        this.accountId = order.getAccountId();
        this.account = order.getAccount();
        this.assetCode = order.getAssetCode();
        this.orderNo = order.getOrderNo();
        this.cardName = order.getCardName();
        this.cardNumber = order.getCardNumber();
        this.codeNo = order.getCodeNo();
        this.fileName = order.getFileName();
        this.number = order.getNumber();
        this.realNumber = order.getRealNumber();
        this.msg = order.getMsg();
        this.status = order.getStatus();
        this.time = order.getTime();
        this.fee = order.getFee();
        this.createDate = order.getCreateDate();
        this.updateDate = order.getUpdateDate();
        this.watchful = order.getWatchful();
        this.version = order.getVersion();
    }
}