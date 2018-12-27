package com.gop.asset.service;


import com.gop.domain.ConfigAssetInfo;

public interface ConfigAssetInfoService {
	
	public ConfigAssetInfo getAssetConfig(String assetCode);

	public void saveConfigAsset(ConfigAssetInfo configAssetInfo);

}
