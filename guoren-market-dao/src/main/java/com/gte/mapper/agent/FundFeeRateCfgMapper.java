package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRateCfg;

public interface FundFeeRateCfgMapper {
    int deleteByPrimaryKey(Long feeRateCfgId);

    int insert(FundFeeRateCfg record);

    int insertSelective(FundFeeRateCfg record);

    FundFeeRateCfg selectByPrimaryKey(Long feeRateCfgId);

    int updateByPrimaryKeySelective(FundFeeRateCfg record);

    int updateByPrimaryKey(FundFeeRateCfg record);
}