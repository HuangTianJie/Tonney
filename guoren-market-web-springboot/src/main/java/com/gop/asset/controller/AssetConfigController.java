package com.gop.asset.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.service.ConfigAssetInfoService;
import com.gop.asset.service.ConfigAssetService;
import com.gop.coin.transfer.dto.WithdrawCoinFeeQueryDto;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetInfo;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.match.service.ConfigSymbolService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;

@RestController
@RequestMapping("/asset-config")
public class AssetConfigController {

	@Autowired
	private ConfigAssetProfileService configAssetProfileService;

	@Autowired
	private ConfigAssetService configAssetService;

	@Autowired
	private ConfigSymbolService configSymbolService;

	@Autowired
	private ConfigAssetInfoService configAssetInfoService;

	@RequestMapping(value = "/asset-config-list", method = RequestMethod.GET)
	public List<WithdrawCoinFeeQueryDto> withdrawCoinFeeConfigList(@AuthForHeader AuthContext context,
			@RequestParam("key") ConfigAssetType key,
			@RequestParam(value = "assetCode", required = false) String assetCode) {

		// 设置返回list
		List<WithdrawCoinFeeQueryDto> dtoList = new ArrayList<>();

		// 查询数据库中的对应list
		List<ConfigAssetProfile> list = configAssetProfileService.getConfigAssetProfileList(key);

		// 转换查询list为dtolist
		if (null != list) {
			dtoList = list.stream().map(a -> new WithdrawCoinFeeQueryDto(a)).collect(Collectors.toList());
		} else {
			dtoList = Collections.emptyList();
		}
		return dtoList;
	}

	@RequestMapping(value = "/configassetinfo", method = RequestMethod.GET)
	public ConfigAssetInfo configassetinfo(@RequestParam("asset") String asset) {
		ConfigAsset info = configAssetService.getAssetConfig(asset);
		ConfigAssetInfo configAssetInfo = configAssetInfoService.getAssetConfig(asset);
		configAssetInfo.setDescription(info.getDescription());
		configAssetInfo.setCnName(info.getName());
		return configAssetInfo;
	}

	@RequestMapping(value = "/appconfigasset", method = RequestMethod.GET)
	public  Map<String, Object> appconfigasset() {
		Map<String, Object> map = new HashMap<String, Object>();
		String str="";
		List<ConfigSymbol> list = configSymbolService.getAllAssetCode1();
		for(int i=0;i<list.size();i++) {
			if(i==0) {
				str=list.get(i).getAssetCode1();
			}else {
				str=str+","+list.get(i).getAssetCode1();
			}
			String str2="";
			List<ConfigSymbol> list2= configSymbolService.getAllAssetCode2(list.get(i).getAssetCode1());
			for(int j=0;j<list2.size();j++) {
				if(j==0) {
					str2=list2.get(j).getAssetCode2();
				}else {
					str2=str2+","+list2.get(j).getAssetCode2();
				}
			}
			map.put(list.get(i).getAssetCode1(), str2);
		}
		map.put("AssetCode", str);
		return map;
	}
}
