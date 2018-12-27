package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashOrderDepositUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CashOrderDepositUserMapper {
    /**
     *
     * @mbggenerated 2018-12-06
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insert(CashOrderDepositUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int insertSelective(CashOrderDepositUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    CashOrderDepositUser selectByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKeySelective(CashOrderDepositUser record);

    /**
     *
     * @mbggenerated 2018-12-06
     */
    int updateByPrimaryKey(CashOrderDepositUser record);

    List<CashOrderDepositUser> selectBySelective(CashOrderDepositUser dao);

    /**
     * 根据订单号查询订单
     * @param orderNo
     * @return
     */
    CashOrderDepositUser selectByOrderNo(@Param("orderNo")String orderNo);

    int updateByPrimaryKeySelectiveOptimisticLocking(CashOrderDepositUser build);
}