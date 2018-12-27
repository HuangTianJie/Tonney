package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRuleCfgHis;

public interface FundFeeRuleCfgHisMapper {
    int deleteByPrimaryKey(Long feeRuleCfgHisId);

    int insert(FundFeeRuleCfgHis record);

    int insertSelective(FundFeeRuleCfgHis record);

    FundFeeRuleCfgHis selectByPrimaryKey(Long feeRuleCfgHisId);

    int updateByPrimaryKeySelective(FundFeeRuleCfgHis record);

    int updateByPrimaryKey(FundFeeRuleCfgHis record);
}