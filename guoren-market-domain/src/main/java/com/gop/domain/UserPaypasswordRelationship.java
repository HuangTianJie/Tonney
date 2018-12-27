package com.gop.domain;

import java.util.Date;

import lombok.ToString;

@ToString
public class UserPaypasswordRelationship {
    private Integer uid;

    private Integer inputType;

    private Date updateDate;

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getInputType() {
		return inputType;
	}

	public void setInputType(Integer inputType) {
		this.inputType = inputType;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
    
    

}