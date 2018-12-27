package com.gop.mapper;

import com.gop.domain.Finance;
import com.gop.domain.StatisticeResult;
import com.gop.domain.enums.CurrencyType;
import com.gop.mapper.dto.FinanceAmountDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FinanceMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(Finance record);

	int insertSelective(Finance record);

	Finance selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(Finance record);

	int updateByPrimaryKey(Finance record);

	/*
	 * 新增接口
	 */
	Finance selectByUidAndAssetCode(@Param("uid") Integer uid, @Param("assetCode") String assetCode);
	
	List<Finance> selectByBrokerId(@Param("brokerId") Integer brokerId,@Param("uid") Integer uid);

	List<Finance> selectByUid(@Param("uid") Integer uid);
	
	Finance selectByAccountNo(@Param("accountNo") String accountNo);

	Integer selectIdByAccountNo(@Param("accountNo") String accountNo);

	/**
	 * 创建排他锁
	 */
	Finance selectForUpdate(@Param("id") Integer id);
	
	Finance selectByUidAndAssetCodeForUpdate(@Param("uid") Integer uid, @Param("assetCode") String assetCode);
	
	int updateFinanceWithVersion(Finance record);
	
	List<StatisticeResult> queryTotalCountByAssetCode(@Param("assetCode") String assetCode);

	List<FinanceAmountDto> getTotalAccountByAssetCode(@Param("assetCode") String assetCode);

	List<Finance> selectUserAccountList(@Param("uid") Integer uid, @Param("assetCode") String assetCode);

	List<Finance> selectUserAccountListByCurrencyType(@Param("uid") Integer uid, @Param("currencyType") CurrencyType currencyType);

//	/*
//	 * 原有接口
//	 */
//	/**
//	 * 
//	 * @param id
//	 * @return
//	 */
//	Integer getIdByUid(@Param("uid") Integer uid);
//
//	/**
//	 * 
//	 * @param id
//	 * @return
//	 */
//	Finance getFinanceForUpdate(@Param("id") Integer id);
//
//	/**
//	 * 
//	 * @param record
//	 * @return
//	 */
//	int UpdateUserFinanceWithVersion(Finance record);
}