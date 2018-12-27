package com.gop.c2c.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.enums.C2cComplaintStatus;
import com.gop.domain.enums.C2cPayType;
import com.gop.domain.enums.C2cTransOrderStatus;
import com.gop.domain.enums.C2cTransType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2cTransOrderComplaintDetailDto {
	
	//申诉编号
    private String complainId;
    //订单号
    private String orderId;
    //交易单号
    private String transOrderId;
    //申诉用户ID
    private Integer uid;
    //申诉类型
    private C2cTransType complainType;
    //申诉原因
    private String complainReason;
    //买家联系方式
    private String buyPhone;
    //买家联系方式
    private String sellPhone;
    //申诉支付方式
    private C2cPayType payType;
    //渠道流水号
    private String payNo;
    //付款截图
    private String capture;
    //备注
    private String remark;
    //申诉单状态
    private C2cComplaintStatus status;
    //订单状态
    private C2cTransOrderStatus transOrderStatus;
    //操作人uid
    private Integer operUid;
    //创建时间
    private Date createDate;
    //更新时间
    private Date updateDate;
    //订单信息
    //交易金额
    private BigDecimal money;
    //交易数量
    private BigDecimal number;
    //订单付款方式
    private String transPayType;
    //交易币种
    private String assetCode;
    //付款账号详情
    private C2cBasePayChannelDto c2cBasePayChannelDto;
	
}
