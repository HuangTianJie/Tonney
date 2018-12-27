package com.gop.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDetail<T> {

    private T data;

    private Integer count;

    private Integer currentPage;

    private Integer pageSize;

    private Integer totalPage;
    
	public void setTotalPage() {
	if(this.count == 0)
		this.totalPage = 0;
	else
		this.totalPage = (this.count  +  this.pageSize  - 1) / this.pageSize;
}
    
    /**
     * 法币类型
     */
    private String hugeType;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public String getHugeType() {
        return hugeType;
    }

    public void setHugeType(String hugeType) {
        this.hugeType = hugeType;
    }
}
