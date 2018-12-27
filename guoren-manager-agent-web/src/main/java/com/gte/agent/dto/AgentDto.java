package com.gte.agent.dto;

import com.gop.domain.enums.LockStatus;
import com.gte.domain.agent.Agent;
import lombok.Data;

import java.util.Date;

@Data
public class AgentDto {

    /**
     * 管理员id
     */
    private Integer adminId;

    /**
     * 代理商名称
     */
    private String agentName;

    private String businessLicence;
    private String url;
    private String code;
    private Integer ruleId;
    private Integer day;


    /**
     * 锁定状态，0-锁定，1-未锁定
     */
    private LockStatus locked;

    /**
     * 管理员名称
     */
    private String opName;

    private String mobile;

    private String loginPassword;

    private Date createDate;

    private String createip;

    private Date updateDate;

    private String updateip;

    private Integer createAdminId;

    public AgentDto(Agent admin) {
        if (admin != null) {
            this.setAdminId(admin.getAdminId());
            this.setAgentName(admin.getAgentName());
            this.setBusinessLicence(admin.getBusinessLicence());
            this.setUrl(admin.getUrl());
            this.setCode(admin.getCode());
            this.setRuleId(admin.getRuleId());
            this.setDay(admin.getDay());
            this.setCreateAdminId(admin.getCreateAdminId());
            this.setCreateDate(admin.getCreateDate());
            this.setCreateip(admin.getCreateip());
            this.setLocked(admin.getLocked());
            this.setLoginPassword(admin.getLoginPassword());
            this.setMobile(admin.getMobile());
            this.setOpName(admin.getOpName());
            this.setUpdateDate(admin.getUpdateDate());
            this.setUpdateip(admin.getUpdateip());
        }
    }
}
