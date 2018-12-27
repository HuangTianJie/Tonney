package com.gop.c2c.dto;

import java.util.Date;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class C2cWechatpayInfoDto extends C2cBasePayChannelDto{

	//用户id
    private Integer uid;
    private String tag;
    private String name;
    private Date createDate;
    private Date updateDate;
    private Integer paytype;
}
