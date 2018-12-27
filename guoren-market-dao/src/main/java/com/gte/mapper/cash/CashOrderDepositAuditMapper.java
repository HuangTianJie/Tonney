package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashOrderDepositAudit;

import java.util.List;

public interface CashOrderDepositAuditMapper {
    /**
     *
     * @mbggenerated 2018-12-06
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insert(CashOrderDepositAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insertSelective(CashOrderDepositAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    CashOrderDepositAudit selectByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKeySelective(CashOrderDepositAudit record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKey(CashOrderDepositAudit record);

    List<CashOrderDepositAudit> selectByOrderNo(String orderNo);
}