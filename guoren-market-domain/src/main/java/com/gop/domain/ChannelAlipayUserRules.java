package com.gop.domain;

import java.util.Date;

import com.gop.domain.enums.SwitchStatus;

import lombok.Data;

@Data
public class ChannelAlipayUserRules {
    private Integer id;

    private String accountNo;

    private String accountName;

    private String accountCode;

    private String rules;

    private SwitchStatus status;

    private String remark;

    private Integer createUser;

    private Date createDate;

    private Integer updateAdminId;

    private Date updateDate;

}