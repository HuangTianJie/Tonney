package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRuleParm;

public interface FundFeeRuleParmMapper {
    int deleteByPrimaryKey(Long feeRuleParmId);

    int insert(FundFeeRuleParm record);

    int insertSelective(FundFeeRuleParm record);

    FundFeeRuleParm selectByPrimaryKey(Long feeRuleParmId);

    int updateByPrimaryKeySelective(FundFeeRuleParm record);

    int updateByPrimaryKey(FundFeeRuleParm record);
}