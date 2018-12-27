package com.gte.cash.service;

import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;

import java.util.List;

public interface CashOrderWithdrawUserFeeService {

    int insertBatch(List<CashOrderWithdrawUserFee> financeDetails);
    List<CashOrderWithdrawUserFee> selectByOrderNo(String orderNo);
    int deleteByOrderNo(String orderNo);
}
