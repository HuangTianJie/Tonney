package com.gop.finance.service.impl;

import com.gop.domain.UserStatistics;
import com.gop.finance.service.UserStatisticsService;
import com.gop.mapper.UserStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
/**
 * Created by wuyanjie on 2018/5/21.
 */
@Service
@Slf4j
public class UserStatisticsServiceImpl implements UserStatisticsService{
    @Autowired
    private UserStatisticsMapper userStatisticsMapper;

    @Override
    public List<UserStatistics> selectUserStatistics() {
        return userStatisticsMapper.selectUserStatistics();
    }

    @Override
    public void insertStatistics(UserStatistics userStatistics) {
        userStatisticsMapper.insertStatistics(userStatistics);
    }

    @Override
    public int countUserStatistics(Date createDate) {
        return userStatisticsMapper.countUserStatistics(createDate);
    }

    @Override
    public int updateStatus(Date createDate) {
        return userStatisticsMapper.updateStatus(createDate);
    }
}
