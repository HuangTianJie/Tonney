package com.gop.domain;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

import com.gop.domain.enums.LockStatus;

@Data
@ToString
public class Administrators {
	
    private Integer adminId;

    private String opName;

    private String mobile;

    private String loginPassword;

    private LockStatus locked;

    private String role;

    private Date createDate;

    private String createip;

    private Date updateDate;

    private String updateip;

    private Integer createAdminId;

}