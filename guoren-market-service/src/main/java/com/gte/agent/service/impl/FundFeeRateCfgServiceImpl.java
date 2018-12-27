package com.gte.agent.service.impl;

import com.gte.agent.service.FundFeeRateCfgService;
import com.gte.domain.agent.FundFeeRateCfg;
import com.gte.mapper.agent.FundFeeRateCfgMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

public class FundFeeRateCfgServiceImpl implements FundFeeRateCfgService {

    @Resource
    private FundFeeRateCfgMapper fundFeeRateCfgMapper;

    @Transactional
    public int insert(FundFeeRateCfg fundFeeRateCfg) {
        return fundFeeRateCfgMapper.insert(fundFeeRateCfg);
    }

    @Transactional
    public int updateByPrimaryKey(FundFeeRateCfg fundFeeRateCfg) {
        return fundFeeRateCfgMapper.updateByPrimaryKey(fundFeeRateCfg);
    }

    @Transactional
    public int updateByPrimaryKeySelective(FundFeeRateCfg fundFeeRateCfg) {
        return fundFeeRateCfgMapper.updateByPrimaryKeySelective(fundFeeRateCfg);
    }

    public FundFeeRateCfg selectByPrimaryKey(Long feeRateCfgId){
        FundFeeRateCfg fundFeeRateCfg = fundFeeRateCfgMapper.selectByPrimaryKey(feeRateCfgId);
        return fundFeeRateCfg;
    }



}
