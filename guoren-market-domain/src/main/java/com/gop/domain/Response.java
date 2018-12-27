package com.gop.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 8803852598712834270L;

    /*
     * 返回状态码
     * */
    private String status = "0000";

//    /*
//     * 多语言的 key
//     * */
//    @JsonIgnore
//    private String msgKey;

    /*
     * 返回字符串
     * */
    private String message = "成功";

    /**
     * 返回结果对象
     */
    private ResponseDetail<T> result;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
    
    public void setStatus(String status){
    	this.status = status;
    }
     
    
//    public void setStatus(BasisEnum status){
//        this.status = status.code();
//        this.message = status.message();
//    }

//    public void setStatus(String status) {
//        this.status = status;
//        this.msgKey = status;
//    }

//    public String getMsgKey() {
//        return this.msgKey;
//    }

//    public void setMsgKey(String msgKey) {
//        this.msgKey = msgKey;
//        this.status = msgKey;
//    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseDetail<T> getResult() {
        return result;
    }

    public void setResult(ResponseDetail<T> result) {
        this.result = result;
    }

    public void SetResponseDetail(T data) {
        if (result == null) {
            result = new ResponseDetail<T>();
        }
        result.setData(data);
    }

    public void SetResponseDetail(T data, Integer count, Integer currentPage, Integer pageSize, Integer totalPage, String hugeType) {
        SetResponseDetail(data);
        result.setCount(count);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        result.setTotalPage(totalPage);
        result.setHugeType(hugeType);
    }

    //    /**
//     * 法币类型
//     */
//    private String hugeType;
//    /*
//     * 返回结果集
//     * */
//    private T result;
//
//    private Integer count;
//
//    private Integer currentPage;
//
//    private Integer pageSize;
//
//    private Integer totalPage;
//
//    public Integer getPageSize() {
//		return pageSize;
//	}
//
//	public void setPageSize(Integer pageSize) {
//		this.pageSize = pageSize;
//	}
//
//	public Integer getTotalPage() {
//		return totalPage;
//	}
//
//	public void setTotalPage() {
//		if(this.count == 0)
//			this.totalPage = 0;
//		else
//			this.totalPage = (this.count  +  this.pageSize  - 1) / this.pageSize;
//	}
//
//	public String getHugeType() {
//        return hugeType;
//    }
//
//    public void setHugeType(String hugeType) {
//        this.hugeType = hugeType;
//    }
//
//    public Response() {
//        this.status = BasisEnum.SUCCESS.code();
//        this.message = BasisEnum.SUCCESS.message();
//    }
//
//    public Integer getCount() {
//        return count;
//    }
//
//    public void setCount(Integer count) {
//        this.count = count;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public T getResult() {
//        return result;
//    }
//
//    public void setResult(T result) {
//        this.result = result;
//    }
//
//	public Integer getCurrentPage() {
//		return currentPage;
//	}
//
//	public void setCurrentPage(Integer currentPage) {
//		this.currentPage = currentPage;
//	}


}
