package com.gte.mapper.cash;

import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CashOrderWithdrawUserFeeMapper {
    /**
     *
     * @mbggenerated 2018-12-14
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *
     * @mbggenerated 2018-12-14
     */
    int insert(CashOrderWithdrawUserFee record);

    /**
     *
     * @mbggenerated 2018-12-14
     */
    int insertSelective(CashOrderWithdrawUserFee record);

    /**
     *
     * @mbggenerated 2018-12-14
     */
    CashOrderWithdrawUserFee selectByPrimaryKey(Integer id);

    List<CashOrderWithdrawUserFee> selectByOrderNo(@Param("orderNo") String orderNo);

    int deleteByOrderNo(@Param("orderNo") String orderNo);
    /**
     *
     * @mbggenerated 2018-12-14
     */
    int updateByPrimaryKeySelective(CashOrderWithdrawUserFee record);

    /**
     *
     * @mbggenerated 2018-12-14
     */
    int updateByPrimaryKey(CashOrderWithdrawUserFee record);

    int insertBatch(List<CashOrderWithdrawUserFee> financeDetails);
}