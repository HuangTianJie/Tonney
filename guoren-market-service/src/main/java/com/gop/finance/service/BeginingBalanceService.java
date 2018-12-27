package com.gop.finance.service;

import com.github.pagehelper.PageInfo;
import com.gop.domain.BeginingBalance;
import com.gop.domain.UserBeginingBalance;

import java.util.Date;
import java.util.List;

/**
 * Created by wuyanjie on 2018/4/28.
 */
public interface BeginingBalanceService {
    List<BeginingBalance> getBeginingBalance(String assetCode,Date startDate);

    void insertBatch(List<BeginingBalance> beginingBalanceList);

    PageInfo<BeginingBalance> queryBeginingBalance(String assetCode, Date startDate,int pageNo, int pageSize );

    PageInfo<UserBeginingBalance> getUserBeginingBalance(Integer uid,String accountNo,String assetCode,Date startDate,int pageNo, int pageSize );

    void insertUserBatch(List<UserBeginingBalance> userBeginingBalances);

    int countUserBeginBalance(Date date);

    void updateUserBeginBalanceStatus(Date date);


}
