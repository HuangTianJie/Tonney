package com.gte.domain;

import lombok.ToString;


@ToString
public class CountryArea {
	
	
	private Integer countryid;
	private String countrycode;
	private String countryname;
	private String countrynum;
	private String ftname;
	private String engname;
	private Integer status;
	public Integer getCountryid() {
		return countryid;
	}
	public void setCountryid(Integer countryid) {
		this.countryid = countryid;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getCountrycode() {
		return countrycode;
	}
	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}
	public String getCountryname() {
		return countryname;
	}
	public void setCountryname(String countryname) {
		this.countryname = countryname;
	}
	public String getCountrynum() {
		return countrynum;
	}
	public void setCountrynum(String countrynum) {
		this.countrynum = countrynum;
	}
	public String getFtname() {
		return ftname;
	}
	public void setFtname(String ftname) {
		this.ftname = ftname;
	}
	public String getEngname() {
		return engname;
	}
	public void setEngname(String engname) {
		this.engname = engname;
	}
	
}
