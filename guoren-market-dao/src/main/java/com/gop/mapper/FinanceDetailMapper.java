package com.gop.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.gop.domain.FinanceDetail;

public interface FinanceDetailMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(FinanceDetail record);

	int insertSelective(FinanceDetail record);

	FinanceDetail selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(FinanceDetail record);

	int updateByPrimaryKey(FinanceDetail record);

	/*
	 * 新增方法
	 */
	List<FinanceDetail> selectByUidAndAssetCode(@Param("uid") Integer uid, @Param("assetCode") String assetCode);

	List<FinanceDetail> selectByUidAndType(@Param("uid") Integer uid, @Param("flag") int flag);

	void insertBatch(List<FinanceDetail> financeDetails);

	//
	// /*
	// * 原有代码
	// */
	// List<FinanceDetail> getListPageBill(@Param("uid") Integer uid);
}