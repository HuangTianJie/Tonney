package com.gop.mapper;


import org.apache.ibatis.annotations.Param;

import com.gop.domain.ConfigAssetInfo;

public interface ConfigAssetInfoMapper {

	int insertSelective(ConfigAssetInfo configAssetInfo);

	ConfigAssetInfo selectByAssetCode(@Param("assetCode") String assetCode);

	
}