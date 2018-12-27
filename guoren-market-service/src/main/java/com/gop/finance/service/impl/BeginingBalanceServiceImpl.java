package com.gop.finance.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.gop.domain.BeginingBalance;
import com.gop.domain.UserBeginingBalance;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.mapper.BeginingBalanceMapper;
import com.gop.mapper.UserBeginingBalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by wuyanjie on 2018/4/28.
 */
@Service
@Slf4j
public class BeginingBalanceServiceImpl implements BeginingBalanceService{
    @Autowired
    private BeginingBalanceMapper beginingBalanceMapper;
    @Autowired
    private UserBeginingBalanceMapper userBeginingBalanceMapper;

    @Override
    public List<BeginingBalance> getBeginingBalance(String assetCode, Date startDate) {
        return beginingBalanceMapper.getBeginingBalance(assetCode,startDate);
    }

    @Override
    public void insertBatch(List<BeginingBalance> beginingBalanceList) {
        beginingBalanceMapper.insertBatch(beginingBalanceList);
    }

    @Override
    public PageInfo<BeginingBalance> queryBeginingBalance(String assetCode, Date startDate, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        return new PageInfo<>(beginingBalanceMapper.getBeginingBalance(assetCode,startDate));
    }

    @Override
    public PageInfo<UserBeginingBalance> getUserBeginingBalance(Integer uid, String accountNo, String assetCode, Date startDate, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        return new PageInfo<>(userBeginingBalanceMapper.getUserBeginingBalance(uid,accountNo,assetCode,startDate));
    }

    @Override
    public void insertUserBatch(List<UserBeginingBalance> userBeginingBalances) {
        userBeginingBalanceMapper.insertBatch(userBeginingBalances);
    }

    @Override
    public int countUserBeginBalance(Date date) {
        return userBeginingBalanceMapper.countUserBeginBalance(date);
    }

    @Override
    public void updateUserBeginBalanceStatus(Date date) {
        userBeginingBalanceMapper.updateStatus(date);
    }
}
