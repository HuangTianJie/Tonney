package com.gop.asset.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.asset.service.ConfigAssetInfoService;
import com.gop.domain.ConfigAssetInfo;
import com.gop.exception.AppException;
import com.gop.mapper.ConfigAssetInfoMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigAssetInfoServiceImpl implements ConfigAssetInfoService {

	@Autowired
	private ConfigAssetInfoMapper configAssetInfoMapper;


	@Override
	public ConfigAssetInfo getAssetConfig(String assetCode) {
		try {
			return configAssetInfoMapper.selectByAssetCode(assetCode);
		} catch (Exception e) {
			log.error("币种" + assetCode + "查询失败", e);
			throw new AppException("币种为:" + assetCode + ",查询失败");
		}
	}


	@Override
	public void saveConfigAsset(ConfigAssetInfo configAssetInfo) {
		try {
			configAssetInfoMapper.insertSelective(configAssetInfo);
		} catch (Exception e) {
			log.error("已有币种" + configAssetInfo.getAssetCode() + "保存失败", e);
			throw new AppException("币种为:" + configAssetInfo.getAssetCode() + ",保存失败");
		}

	}

}
