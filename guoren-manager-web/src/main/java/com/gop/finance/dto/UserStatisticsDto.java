package com.gop.finance.dto;

import com.gop.domain.UserStatistics;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by wuyanjie on 2018/5/22.
 */
@Data
public class UserStatisticsDto {
    private BigDecimal totalUser;  //总量
    private BigDecimal nativeUser;  //初级实名
    private BigDecimal advancedUser; //高级实名用户数
    private String nativePresent;
    private String advancedPresent;
    private Date createDate;

    public UserStatisticsDto(UserStatistics userStatistics){
        this.totalUser = userStatistics.getTotalUser();
        this.nativeUser = userStatistics.getNativeUser();
        this.advancedUser = userStatistics.getAdvancedUser();
        this.nativePresent = userStatistics.getNativePresent().multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP) + "%";
        this.advancedPresent = userStatistics.getAdvancedPresent().multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP) + "%";
        this.createDate = userStatistics.getCreateDate();
    }
}
