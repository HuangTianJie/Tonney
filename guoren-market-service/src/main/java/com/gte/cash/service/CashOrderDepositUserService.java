package com.gte.cash.service;

import com.gop.mode.vo.PageModel;
import com.gte.domain.cash.dto.CashOrderDepositAudit;
import com.gte.domain.cash.dto.CashOrderDepositUser;

import java.math.BigDecimal;

public interface CashOrderDepositUserService {


    int insertSelective(CashOrderDepositUser record);

    CashOrderDepositUser selectByPrimaryKey(Integer id);

    CashOrderDepositUser selectByOrderNo(String orderNo);

    /**
     * 更新
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKeySelective(CashOrderDepositUser record);

    /**
     * 提币审核-通用功能
     * @param record
     * @return
     */
    int approval(CashOrderDepositAudit record, CashOrderDepositUser order, BigDecimal realNumber);

    PageModel<CashOrderDepositUser> selectBySelective(CashOrderDepositUser dao, Integer pageNo, Integer pageSize);
}