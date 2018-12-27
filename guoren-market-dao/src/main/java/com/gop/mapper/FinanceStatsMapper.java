package com.gop.mapper;

import com.gop.domain.FinanceStats;

public interface FinanceStatsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FinanceStats record);

    int insertSelective(FinanceStats record);

    FinanceStats selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FinanceStats record);

    int updateByPrimaryKey(FinanceStats record);
}