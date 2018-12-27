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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "deposit对象", description = "充值订单对象")
public class CashOrderDepositUserDto {

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
    @NotNull
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
    @NotNull
    @ApiModelProperty(value = "户名",example="king")
    private String cardName;

    /**
     * 卡号
     */
    @NotNull
    @ApiModelProperty(value = "卡号",example="123123123123123a41231")
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
    @ApiModelProperty(hidden = true)
    private String orderNo;
    /**
     * 识别码
     */
    @NotNull
    @ApiModelProperty(value = "识别码",example="00001F")
    private String codeNo;

    /**
     * 凭证
     */
    @NotNull
    @ApiModelProperty(value = "凭证",example="icbc.png")
    private String fileName;

    /**
     * 汇款时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    @ApiModelProperty(value = "汇款时间",example="2018-12-12")
    private Date time;

    /**
     * 总数：实际数+手续费
     */
	@NotNull
    @ApiModelProperty(value = "总数：实际数+手续费",example="11")
    private BigDecimal number;

    /**
     * 实际数量
     */
    @ApiModelProperty(hidden = true)
    private BigDecimal realNumber;

    /**
     * 消息
     */
    @ApiModelProperty(value = "消息",example="init")
    private String msg;

    /**
     * 订单状态：待查账，待审核，审核通过，充值失败
     */
    @ApiModelProperty(hidden = true)
    private DepositStatus status;

    /**
     * 充值手续费
     */
    @ApiModelProperty(hidden = true)
    private BigDecimal fee;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

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
        if(DepositStatus.CHECK.equals(order.getStatus())){
            this.status = DepositStatus.WAITAUDIT;
        }else{
            this.status = order.getStatus();
        }

        this.time = order.getTime();
        this.fee = order.getFee();
        this.createDate = order.getCreateDate();
    }
}