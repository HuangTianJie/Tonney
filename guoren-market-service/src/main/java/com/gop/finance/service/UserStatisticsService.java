package com.gop.finance.service;

import com.gop.domain.UserStatistics;

import java.util.Date;
import java.util.List;

/**
 * Created by wuyanjie on 2018/5/21.
 */
public interface UserStatisticsService {
    List<UserStatistics> selectUserStatistics();

    void insertStatistics(UserStatistics userStatistics);

    int countUserStatistics(Date createDate);

    int updateStatus(Date createDate);
}
