package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CashOrderWithdrawUserMapper {
    /**
     *
     * @mbggenerated 2018-12-06
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insert(CashOrderWithdrawUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insertSelective(CashOrderWithdrawUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    CashOrderWithdrawUser selectByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKeySelective(CashOrderWithdrawUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKey(CashOrderWithdrawUser record);

    List<CashOrderWithdrawUser> selectBySelective(CashOrderWithdrawUser dao);

    CashOrderWithdrawUser selectByOrderNo(@Param("orderNo")String orderNo);

    int updateByPrimaryKeySelectiveOptimisticLocking(CashOrderWithdrawUser record);
}