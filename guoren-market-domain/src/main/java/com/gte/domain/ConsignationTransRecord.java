package com.gte.domain;

import java.util.Date;

import lombok.ToString;

@ToString
public class ConsignationTransRecord {
	
	private Integer id;
	private String consignationId;
	private Date createDate;
	private String sell;
	private String duad;
	private Integer type;
	private Integer consignationNum;
	private String consignationPrice;
	private Integer residueNum;
	private Integer status;
	private Date createTime;
	private Date updateTime;
	private String createId;
	private Date startDate;
	private Date endDate;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getConsignationId() {
		return consignationId;
	}
	public void setConsignationId(String consignationId) {
		this.consignationId = consignationId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getSell() {
		return sell;
	}
	public void setSell(String sell) {
		this.sell = sell;
	}
	public String getDuad() {
		return duad;
	}
	public void setDuad(String duad) {
		this.duad = duad;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getConsignationNum() {
		return consignationNum;
	}
	public void setConsignationNum(Integer consignationNum) {
		this.consignationNum = consignationNum;
	}
	public String getConsignationPrice() {
		return consignationPrice;
	}
	public void setConsignationPrice(String consignationPrice) {
		this.consignationPrice = consignationPrice;
	}
	public Integer getResidueNum() {
		return residueNum;
	}
	public void setResidueNum(Integer residueNum) {
		this.residueNum = residueNum;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getCreateId() {
		return createId;
	}
	public void setCreateId(String createId) {
		this.createId = createId;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	
}
