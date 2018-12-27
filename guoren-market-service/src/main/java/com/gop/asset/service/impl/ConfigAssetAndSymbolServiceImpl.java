package com.gop.asset.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.asset.service.ConfigAssetAndSymbolService;
import com.gop.asset.service.ConfigAssetService;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.ConfigSymbolProfile;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.ConfigSymbolType;
import com.gop.match.service.ConfigSymbolProfileService;
import com.gop.match.service.ConfigSymbolService;

@Service("ConfigAssetAndSymbolService")
public class ConfigAssetAndSymbolServiceImpl implements ConfigAssetAndSymbolService {
	// 币种
	private static final String WITHDRAWLEVEL = "-1";
	private static final String WITHDRAWPRECISION = "5";
	private static final String ASSETCODECONFIRMNUM = "0";
	private static final String WITHDRAWMIN = "0.1";
	private static final String WITHDRAWMAX = "50000";
	private static final String WITHDRAWMINFEE = "0.001";
	// 交易对
	private static final String ASSETFEERATE = "0";
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private ConfigSymbolService configSymbolService;
	@Autowired
	private ConfigAssetProfileService configAssetProfileService;
	@Autowired
	private ConfigSymbolProfileService configSymbolProfileService;

	@Override
	@Transactional
	public void addConfigAssetAndInitProfile(ConfigAsset configAsset) {
		configAssetService.saveConfigAsset(configAsset);
		// 针对新币种初始化config_asset_profile中withdrawlevel0-2的值为-1
		ConfigAssetProfile configAssetProfile = new ConfigAssetProfile();
		configAssetProfile.setAssetCode(configAsset.getAssetCode());
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWLEVEL_0);
		configAssetProfile.setProfileValue(WITHDRAWLEVEL);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWLEVEL_1);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWLEVEL_2);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.DEPOSITLEVEL_DEFAULT);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWPRECISION);
		configAssetProfile.setProfileValue(WITHDRAWPRECISION);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.ASSETCODECONFIRMNUM);
		configAssetProfile.setProfileValue(ASSETCODECONFIRMNUM);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWMIN);
		configAssetProfile.setProfileValue(WITHDRAWMIN);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWMAX);
		configAssetProfile.setProfileValue(WITHDRAWMAX);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
		configAssetProfile.setProfileKey(ConfigAssetType.WITHDRAWMINFEE);
		configAssetProfile.setProfileValue(WITHDRAWMINFEE);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
	}

	@Override
	@Transactional
	public void addConfigSymbolAndInitProfile(ConfigSymbol configSymbol) {
		configSymbolService.saveConfigAsset(configSymbol);
		ConfigSymbolProfile configSymbolProfile = new ConfigSymbolProfile();
		configSymbolProfile.setSymbol(configSymbol.getSymbol());
		configSymbolProfile.setProfileKey(ConfigSymbolType.ASSETFEERATE);
		configSymbolProfile.setProfileValue(ASSETFEERATE);
		configSymbolProfileService.createOrUpdateConfigSymbolProfile(configSymbolProfile);
	}

	@Override
	@Transactional
	public void addConfigSymbolAndInitProfile(ConfigSymbol configSymbol, List<ConfigSymbolProfile> list) {
		addConfigSymbolAndInitProfile(configSymbol);
		if (list.size() > 0) {
			list.stream().forEach(profille -> configSymbolProfileService.createOrUpdateConfigSymbolProfile(profille));
		}

	}
}
