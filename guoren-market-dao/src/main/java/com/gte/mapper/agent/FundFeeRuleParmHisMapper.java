package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRuleParmHis;

public interface FundFeeRuleParmHisMapper {
    int deleteByPrimaryKey(Long feeRuleParmHisId);

    int insert(FundFeeRuleParmHis record);

    int insertSelective(FundFeeRuleParmHis record);

    FundFeeRuleParmHis selectByPrimaryKey(Long feeRuleParmHisId);

    int updateByPrimaryKeySelective(FundFeeRuleParmHis record);

    int updateByPrimaryKey(FundFeeRuleParmHis record);
}