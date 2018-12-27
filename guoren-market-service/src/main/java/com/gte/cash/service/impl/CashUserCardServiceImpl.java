package com.gte.cash.service.impl;

import com.gte.cash.service.CashUserCardService;
import com.gte.domain.cash.dto.CashUserCard;
import com.gte.mapper.cash.CashUserCardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CashUserCardServiceImpl implements CashUserCardService {

    @Resource
    private CashUserCardMapper mapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return mapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CashUserCard record) {
        return mapper.insert(record);
    }

    @Override
    public int insertSelective(CashUserCard record) {
        return mapper.insertSelective(record);
    }

    @Override
    public CashUserCard selectByPrimaryKey(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CashUserCard record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CashUserCard record) {
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public List<CashUserCard> selectSelective(CashUserCard record) {
        return mapper.selectSelective(record);
    }
}