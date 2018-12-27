package com.gte.cash.service;

import com.gte.domain.cash.dto.CashOrderDepositAudit;

import java.util.List;

public interface CashOrderDepositAuditService {
    int deleteByPrimaryKey(Integer id);

    int insert(CashOrderDepositAudit record);

    /**
     * 添加操作纪录
     * @param record
     * @return
     */
    int insertSelective(CashOrderDepositAudit record);

    CashOrderDepositAudit selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CashOrderDepositAudit record);

    int updateByPrimaryKey(CashOrderDepositAudit record);

    List<CashOrderDepositAudit> selectByOrderNo(String orderNo);
}