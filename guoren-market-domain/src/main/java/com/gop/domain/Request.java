package com.gop.domain;

import java.io.Serializable;

public class Request<T> implements Serializable {

    private static final long serialVersionUID = -1976117257744018398L;

    //Android，IOS 头文件部分数据
//设备号
    private String deviceId;
    //数据来源平台
    private String platform;
    //token，唯一识别码
    private String token;
    //版本号
    private String appVersion;
    //时间戳
    private String timestamp;
    //语言
    private String language;
    //法币类型
    private String hugeType = "CNY";//默认人名币

    //分页页码
    private int pageIndex = 1;//1
    //分页每页记录数
    private int pageSize = 10;//10
    //请求参数
    private T data;
    //用户信息，前端不传递用户信息，在AOP里面根据TOKEN,在redis里面获取
    private User user;

    public String getHugeType() {
        return hugeType;
    }

    public void setHugeType(String hugeType) {
        this.hugeType = hugeType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String plantform) {
        this.platform = plantform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
