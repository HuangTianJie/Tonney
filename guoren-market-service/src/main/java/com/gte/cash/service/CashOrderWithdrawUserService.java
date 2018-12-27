package com.gte.cash.service;

import com.gop.mode.vo.PageModel;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;

import java.util.List;

public interface CashOrderWithdrawUserService {
    int deleteByPrimaryKey(Integer id);

    int insert(CashOrderWithdrawUser record);

    int insertSelective(CashOrderWithdrawUser record);

    CashOrderWithdrawUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CashOrderWithdrawUser record);

    int updateByPrimaryKey(CashOrderWithdrawUser record);

    PageModel<CashOrderWithdrawUser> selectBySelective(CashOrderWithdrawUser dao, Integer pageNo, Integer pageSize);

    CashOrderWithdrawUser selectByOrderNo(String orderNo);

    int approval(CashOrderWithdrawAudit record, CashOrderWithdrawUser order, List<CashOrderWithdrawUserFee> list);
}