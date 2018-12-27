package com.gop.domain;

import java.util.Date;

import com.gop.domain.enums.C2cPayAccountStatus;

import lombok.Data;
@Data
public class C2cWeChatInfo {
    private Integer id;

    private Integer uid;

    private String tag;

    private Integer addIndex;
    
    private C2cPayAccountStatus status;

    private Date createDate;

    private Date updateDate;
}