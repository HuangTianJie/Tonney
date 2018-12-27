package com.gte.user.dto;

import java.util.Date;

import com.gop.domain.Administrators;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AdministratorDto {
	
	String opName;
	
	String account;
	
	String role;
	
	Integer uid;
	String token;
	
	String locked;
	
	Date updateDate;
	
	Date createDate;
	
	Integer createId;
	
	public AdministratorDto(Administrators administrators){
		this.setCreateDate(administrators.getCreateDate());
		this.setCreateId(administrators.getCreateAdminId());
		this.setLocked(administrators.getLocked().toString());
		this.setOpName(administrators.getOpName());
		this.setRole(administrators.getRole().toString());
		this.setUid(administrators.getAdminId());
		this.setUpdateDate(administrators.getUpdateDate());
		this.setAccount(administrators.getMobile());
	}

}
