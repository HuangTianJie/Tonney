package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashOrderWithdrawAudit;

import java.util.List;

public interface CashOrderWithdrawAuditMapper {
    /**
     *
     * @mbggenerated 2018-12-06
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insert(CashOrderWithdrawAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insertSelective(CashOrderWithdrawAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    CashOrderWithdrawAudit selectByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKeySelective(CashOrderWithdrawAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKey(CashOrderWithdrawAudit record);

    List<CashOrderWithdrawAudit> selectByOrderNo(String orderNo);
}