package com.gte.mapper.agent;

import com.gte.domain.agent.FundFeeRateCfgHis;

public interface FundFeeRateCfgHisMapper {
    int deleteByPrimaryKey(Long feeRateCfgHisId);

    int insert(FundFeeRateCfgHis record);

    int insertSelective(FundFeeRateCfgHis record);

    FundFeeRateCfgHis selectByPrimaryKey(Long feeRateCfgHisId);

    int updateByPrimaryKeySelective(FundFeeRateCfgHis record);

    int updateByPrimaryKey(FundFeeRateCfgHis record);
}