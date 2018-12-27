package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashUserCard;

import java.util.List;

public interface CashUserCardMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CashUserCard record);

    int insertSelective(CashUserCard record);

    CashUserCard selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CashUserCard record);

    int updateByPrimaryKey(CashUserCard record);

    List<CashUserCard> selectSelective(CashUserCard record);
}