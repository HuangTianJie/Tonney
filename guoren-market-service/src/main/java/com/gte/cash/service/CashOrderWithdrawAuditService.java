package com.gte.cash.service;

import com.gte.domain.cash.dto.CashOrderWithdrawAudit;

import java.util.List;

public interface CashOrderWithdrawAuditService {
    int deleteByPrimaryKey(Integer id);

    int insert(CashOrderWithdrawAudit record);

    int insertSelective(CashOrderWithdrawAudit record);

    CashOrderWithdrawAudit selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CashOrderWithdrawAudit record);

    int updateByPrimaryKey(CashOrderWithdrawAudit record);

    List<CashOrderWithdrawAudit> selectByOrderNo(String orderNo);
}