package com.gte.cash.service.impl;

import com.gte.cash.service.CashOrderDepositAuditService;
import com.gte.domain.cash.dto.CashOrderDepositAudit;
import com.gte.mapper.cash.CashOrderDepositAuditMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CashOrderDepositAuditServiceImpl implements CashOrderDepositAuditService {

    @Resource
    private CashOrderDepositAuditMapper mapper;

    public int deleteByPrimaryKey(Integer id){
        return mapper.deleteByPrimaryKey(id);
    }

    public int insert(CashOrderDepositAudit record){
        return mapper.insert(record);
    }

    public int insertSelective(CashOrderDepositAudit record){
        return mapper.insertSelective(record);
    }

    public CashOrderDepositAudit selectByPrimaryKey(Integer id){
        return mapper.selectByPrimaryKey(id);
    }

    public int updateByPrimaryKeySelective(CashOrderDepositAudit record){
        return mapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(CashOrderDepositAudit record){
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public List<CashOrderDepositAudit> selectByOrderNo(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }
}