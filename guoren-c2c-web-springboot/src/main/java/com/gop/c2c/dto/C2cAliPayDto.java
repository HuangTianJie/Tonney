package com.gop.c2c.dto;

import java.util.Date;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
@Data
public class C2cAliPayDto {

	//用户id
    private Integer uid;
    //姓名
    @NotBlank
    private String name;
    //支付宝账号
    @NotBlank
    private String alipayNo;
    //创建时间
    private Date createDate;
    //更新时间
    private Date updateDate;
}
