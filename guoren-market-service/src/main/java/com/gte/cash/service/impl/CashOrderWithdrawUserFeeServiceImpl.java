package com.gte.cash.service.impl;

import com.gte.cash.service.CashOrderWithdrawUserFeeService;
import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;
import com.gte.mapper.cash.CashOrderWithdrawUserFeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CashOrderWithdrawUserFeeServiceImpl implements CashOrderWithdrawUserFeeService {

    @Resource
    private CashOrderWithdrawUserFeeMapper mapper;

    @Override
    public int insertBatch(List<CashOrderWithdrawUserFee> financeDetails){
        return mapper.insertBatch(financeDetails);
    }

    @Override
    public List<CashOrderWithdrawUserFee> selectByOrderNo(String orderNo){
        return mapper.selectByOrderNo(orderNo);
    }

    @Override
    public int deleteByOrderNo(String orderNo) {
        return mapper.deleteByOrderNo(orderNo);
    }
}
