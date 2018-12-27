package com.gte.cash.service.impl;

import com.gte.cash.service.CashOrderWithdrawAuditService;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.mapper.cash.CashOrderWithdrawAuditMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CashOrderWithdrawAuditServiceImpl implements CashOrderWithdrawAuditService {

    @Resource
    private CashOrderWithdrawAuditMapper mapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return mapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CashOrderWithdrawAudit record) {
        return mapper.insert(record);
    }

    @Override
    public int insertSelective(CashOrderWithdrawAudit record) {
        return mapper.insertSelective(record);
    }

    @Override
    public CashOrderWithdrawAudit selectByPrimaryKey(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CashOrderWithdrawAudit record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CashOrderWithdrawAudit record) {
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public List<CashOrderWithdrawAudit> selectByOrderNo(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }
}