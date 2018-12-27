package com.gte.domain.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.enums.DepositStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 普通用户法币充值订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOrderDepositUser {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer uid;

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
     * 资产类型
     */
    private String assetCode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 识别码
     */
    private String codeNo;

    /**
     * 凭证
     */
    private String fileName;

    /**
     * 汇款时间
     */
    @JSONField(format = "yyyy-MM-dd")
    private Date time;

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
     * 订单状态：待查账，待审核，审核通过，充值失败
     */
    private DepositStatus status;

    private DepositStatus formStatus;

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
     * 充值手续费
     */
    private BigDecimal fee;

    /**
     * 管理员帐号
     */
    private Integer adminId;

    /**
     * 是否关注
     */
    private Integer watchful;

    /**
     * 版本号
     */
    private Integer version;

}