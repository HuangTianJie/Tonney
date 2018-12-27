package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRuleCfg;

public interface FundFeeRuleCfgMapper {
    int deleteByPrimaryKey(Long feeRuleCfgId);

    int insert(FundFeeRuleCfg record);

    int insertSelective(FundFeeRuleCfg record);

    FundFeeRuleCfg selectByPrimaryKey(Long feeRuleCfgId);

    int updateByPrimaryKeySelective(FundFeeRuleCfg record);

    int updateByPrimaryKey(FundFeeRuleCfg record);
}