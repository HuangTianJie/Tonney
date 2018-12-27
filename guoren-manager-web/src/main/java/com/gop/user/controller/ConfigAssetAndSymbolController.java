package com.gop.user.controller;

import com.google.common.collect.Lists;
import com.gop.asset.service.ConfigAssetAndSymbolService;
import com.gop.asset.service.ConfigAssetInfoService;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.ManagerConfigConst;
import com.gop.code.consts.MatchCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.service.ChannelCoinAddressDepositInfoService;
import com.gop.coin.transfer.service.ChannelCoinAddressDepositPoolService;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.domain.*;
import com.gop.domain.enums.*;
import com.gop.exception.AppException;
import com.gop.match.service.ConfigSymbolProfileService;
import com.gop.match.service.ConfigSymbolService;
import com.gop.match.service.discovery.impl.ZookeeperServiceUrlDiscovery;
import com.gop.user.dto.ConfigAssetDto;
import com.gop.user.dto.ConfigSymbolDto;
import com.gop.user.dto.ConfigSymbolProfileInitDto;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController("ConfigAssetAndSymbolController")
@RequestMapping("/managerAssetAndSymbol")
public class ConfigAssetAndSymbolController {
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private ConfigSymbolService configSymbolService;
	@Autowired
	private ConfigSymbolProfileService configSymbolProfileService;
	@Autowired
	private ChannelCoinAddressDepositPoolService channelCoinAddressDepositPoolService;
	@Autowired
	private ZookeeperServiceUrlDiscovery zookeeperServiceUrlDiscovery;
	@Autowired
	private ConfigAssetProfileService configAssetProfileService;
	@Autowired
	private ConfigAssetAndSymbolService configAssetAndSymbolService;
	@Autowired
	private ChannelCoinAddressDepositInfoService channelCoinAddressDepositInfoService;
	@Autowired
	private ConfigAssetInfoService configAssetInfoService;

	// 添加新币种或更新币种状态
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/configasset-edit", method = RequestMethod.POST)
	public void configAssetCodeCreateOrUpdate(@AuthForHeader AuthContext context,
			@Valid @RequestBody ConfigAssetDto dto) {
		if (dto.getSupplyAmount().compareTo(BigDecimal.ZERO) < 0
				|| dto.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		ConfigAsset configAsset = new ConfigAsset();
		configAsset.setAssetCode(dto.getAssetCode());
		configAsset.setCurrencyType(dto.getCurrencyType());
		configAsset.setStatus(dto.getStatus());
		configAsset.setName(dto.getName());
		configAsset.setSupplyAmount(dto.getSupplyAmount());
		configAsset.setTotalAmount(dto.getTotalAmount());
		// 最小精度默认值为0
		configAsset.setMinPrecision(dto.getMinPrecision() > 0 ? dto.getMinPrecision() : 0);
		// 描述默认是0
		configAsset.setDescription(StringUtils.isNotBlank(dto.getDescription()) ? dto.getDescription() : "0");
		// webUrl默认是0
		configAsset.setWebUrl(StringUtils.isNotBlank(dto.getWebUrl()) ? dto.getWebUrl() : "0");

		ConfigAsset configAssetInDB = configAssetService.getAssetConfig(dto.getAssetCode());
		// 更新已有币种
		// 校验所传入更新的币种,与数据库匹配,且不能修改币的名称(防止关联表出现错误)
		if (null != configAssetInDB) {
			configAsset.setId(configAssetInDB.getId());
			configAssetService.updateConfigAsset(configAsset);
			return;
		}
		// 添加新币种
		configAssetAndSymbolService.addConfigAssetAndInitProfile(configAsset);
		//添加新币详细信息
		ConfigAssetInfo configAssetInfo = new ConfigAssetInfo();
		configAssetInfo.setAssetCode(dto.getAssetCode());
		configAssetInfo.setReleaseTime(dto.getReleaseTime());
		configAssetInfo.setUsedAmount(dto.getUsedAmount());
		configAssetInfo.setReleaseAmount(dto.getReleaseAmount());
		configAssetInfo.setPrice(dto.getPrice());
		configAssetInfo.setInfo(dto.getInfo());
		configAssetInfo.setInfoSearch(dto.getInfoSearch());
		configAssetInfoService.saveConfigAsset(configAssetInfo);
	}

	// 更改已有币种配置
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/configassetprofile-edit", method = RequestMethod.GET)
	public void configAssetProfilecreateOrUpdate(@AuthForHeader AuthContext context,
			@RequestParam("asset") String asset, @RequestParam("key") ConfigAssetType key,
			@RequestParam("value") String value) {
		// 校验value,使用正则表达式
		boolean validateValue = key.validValue(value);

		if (!validateValue) {
			throw new AppException(CommonCodeConst.FIELD_ERROR,"非法的输入参数！");
		}
		if (ConfigAssetType.DEPOSITLEVEL_DEFAULT.equals(key) && Integer.valueOf(value) == 0) {
			ChannelCoinAddressDepositInfo info = channelCoinAddressDepositInfoService
					.getChannelCoinAddressDepositInfoByTargetAssetCode(asset);
			Integer addressAmount = channelCoinAddressDepositPoolService.getAmountByAssetCodeAndAddressStatus(
					null == info ? asset : info.getAddressAssetCode(), CoinAddressStatus.NEW);
			if (0 == addressAmount) {
				throw new AppException(UserAssetCodeConst.DEPOSIT_POOL_COIN_ADDRESS_LESS);
			}
		}
		ConfigAssetProfile configAssetProfile = new ConfigAssetProfile();
		configAssetProfile.setAssetCode(asset);
		configAssetProfile.setProfileKey(key);
		configAssetProfile.setProfileValue(value);
		configAssetProfileService.createOrUpdateConfigAssetProfile(configAssetProfile);
	}

	// 已有币种列表查询
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/configasset-list", method = RequestMethod.GET)
	public List<ConfigAsset> getConfigAssetList(@AuthForHeader AuthContext context,
			@RequestParam(value = "status", required = false) AssetStatus status) {
		List<ConfigAsset> list = configAssetService.getAllAssetCode();
		return list;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/configassetprofile-list", method = RequestMethod.GET)
	public List<ConfigAssetProfile> getConfigAssetProfileList(@AuthForHeader AuthContext context) {
		List<ConfigAssetProfile> list = configAssetProfileService.selectAll();
		return list;
	}

	// 添加新交易对
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/configsymbol-create", method = RequestMethod.POST)
	public void configSymbolCreate(@AuthForHeader AuthContext context,
			@Valid @RequestBody ConfigSymbolProfileInitDto dto) {
		if (dto.getAssetCode1().equals(dto.getAssetCode2())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "币种名称不能一样");
		}
		if (null == dto.getStatus() || !SymbolStatus.INIT.equals(dto.getStatus())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "创建交易对必须是init状态");
		}
		// 传递参数校验
		ConfigAsset assetConfig1 = configAssetService.getAssetConfig(dto.getAssetCode1());
		ConfigAsset assetConfig2 = configAssetService.getAssetConfig(dto.getAssetCode2());
		if (null == assetConfig1 || null == assetConfig2) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传币种不存在");
		}
		if (!dto.getSymbol().equals(assetConfig1.getAssetCode() + "_" + assetConfig2.getAssetCode())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传交易对名称与币种名称不一致");
		}
		if (dto.getMinAmount1().compareTo(BigDecimal.ZERO) < 0 || dto.getMinAmount2().compareTo(BigDecimal.ZERO) < 0
				|| dto.getMinPrecision1() < 0 || dto.getMinPrecision2() < 0
				|| dto.getMaxAmount1().compareTo(dto.getMinAmount1()) < 0
				|| dto.getMaxAmount2().compareTo(dto.getMinAmount2()) < 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传数值不能小于零");
		}
		ConfigSymbol symbolInDB = configSymbolService.getConfigSymbol(dto.getSymbol());
		if (null != symbolInDB) {
			throw new AppException(ManagerConfigConst.SYMBOL_ALREADY_HAVE);
		}
		if (dto.getAmountPrecision() < 0 || dto.getPricePrecision() < 0 || dto.getHighlightNo() < 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "数值不能小于零");
		}

		ConfigSymbol configSymbol = new ConfigSymbol();
		configSymbol.setSymbol(dto.getSymbol());
		configSymbol.setAssetCode1(dto.getAssetCode1());
		configSymbol.setAssetCode2(dto.getAssetCode2());
		configSymbol.setStatus(dto.getStatus());
		configSymbol.setTitle(dto.getTitle());
		configSymbol.setName(dto.getName());
		configSymbol.setDescription(dto.getDescription());
		configSymbol.setMinPrecision1(dto.getMinPrecision1());
		configSymbol.setMinPrecision2(dto.getMinPrecision1());
		configSymbol.setMinAmount1(dto.getMinAmount1());
		configSymbol.setMinAmount2(dto.getMinAmount2());
		configSymbol.setMaxAmount1(dto.getMaxAmount1());
		configSymbol.setMaxAmount2(dto.getMaxAmount2());

		List<ConfigSymbolProfile> list = Lists.newArrayList();
		ConfigSymbolProfile pricePrecision = new ConfigSymbolProfile();
		pricePrecision.setSymbol(dto.getSymbol());
		pricePrecision.setProfileKey(ConfigSymbolType.PRICEPRECISION);
		pricePrecision.setProfileValue(dto.getPricePrecision().toString());
		list.add(pricePrecision);
		ConfigSymbolProfile amountPrecision = new ConfigSymbolProfile();
		amountPrecision.setSymbol(dto.getSymbol());
		amountPrecision.setProfileKey(ConfigSymbolType.AMOUNTPRECISION);
		amountPrecision.setProfileValue(dto.getAmountPrecision().toString());
		list.add(amountPrecision);
		ConfigSymbolProfile highlightNo = new ConfigSymbolProfile();
		highlightNo.setSymbol(dto.getSymbol());
		highlightNo.setProfileKey(ConfigSymbolType.HIGHLIGHTNO);
		highlightNo.setProfileValue(dto.getHighlightNo().toString());
		list.add(highlightNo);
		configAssetAndSymbolService.addConfigSymbolAndInitProfile(configSymbol, list);
	}

	// 添加新交易对或更新交易对状态
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/configsymbol-update", method = RequestMethod.POST)
	public void configSymbolUpdate(@AuthForHeader AuthContext context, @Valid @RequestBody ConfigSymbolDto dto) {
		if (dto.getAssetCode1().equals(dto.getAssetCode2())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "币种名称不能一样");
		}
		// 传递参数校验
		ConfigAsset assetConfig1 = configAssetService.getAssetConfig(dto.getAssetCode1());
		ConfigAsset assetConfig2 = configAssetService.getAssetConfig(dto.getAssetCode2());
		if (null == assetConfig1 || null == assetConfig2) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传币种不存在");
		}
		if (!dto.getSymbol().equals(assetConfig1.getAssetCode() + "_" + assetConfig2.getAssetCode())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传交易对名称与币种名称不一致");
		}
		if (dto.getMinAmount1().compareTo(BigDecimal.ZERO) < 0 || dto.getMinAmount2().compareTo(BigDecimal.ZERO) < 0
				|| dto.getMinPrecision1() < 0 || dto.getMinPrecision2() < 0
				|| dto.getMaxAmount1().compareTo(dto.getMinAmount1()) < 0
				|| dto.getMaxAmount2().compareTo(dto.getMinAmount2()) < 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "所传数值不能小于零");
		}
		// 校验更新状态
		ConfigSymbol symbolInDB = configSymbolService.getConfigSymbol(dto.getSymbol());
		if (null == symbolInDB) {
			throw new AppException(ManagerConfigConst.API_INVOKE_ERROR);
		}
		if (SymbolStatus.LISTED.equals(dto.getStatus())) {
			// 交易对校验,必须为交易对引擎已经启动
			Set<String> fromDiscovery = zookeeperServiceUrlDiscovery.getFromDiscovery(dto.getSymbol());
			if (null == fromDiscovery) {
				throw new AppException(MatchCodeConst.SYMBOL_ENGINER_LESS, "撮合引擎没有开启");
			}
		}
		ConfigSymbol configSymbol = new ConfigSymbol();
		configSymbol.setSymbol(dto.getSymbol());
		configSymbol.setAssetCode1(dto.getAssetCode1());
		configSymbol.setAssetCode2(dto.getAssetCode2());
		configSymbol.setStatus(dto.getStatus());
		configSymbol.setTitle(dto.getTitle());
		configSymbol.setName(dto.getName());
		configSymbol.setDescription(dto.getDescription());
		configSymbol.setMinPrecision1(dto.getMinPrecision1());
		configSymbol.setMinPrecision2(dto.getMinPrecision1());
		configSymbol.setMinAmount1(dto.getMinAmount1());
		configSymbol.setMinAmount2(dto.getMinAmount2());
		configSymbol.setMaxAmount1(dto.getMaxAmount1());
		configSymbol.setMaxAmount2(dto.getMaxAmount2());

		configSymbol.setId(symbolInDB.getId());
		configSymbolService.updateConfigAsset(configSymbol);
	}

	// 更改交易对配置
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/configsymbolprofile-edit", method = RequestMethod.GET)
	public void configSymbolProfilecreateOrUpdate(@AuthForHeader AuthContext context,
			@RequestParam("configSymbol") String symbol, @RequestParam("key") ConfigSymbolType key,
			@RequestParam("value") String value) {
		// 校验value,使用正则表达式
		if (!key.validateValue(value)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		// 校验symbol,并且是当前状态为init与listed
		ConfigSymbol configSymbol = configSymbolService.getConfigSymbol(symbol);
		if (null == configSymbol) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		ConfigSymbolProfile configSymbolProfile = new ConfigSymbolProfile();
		configSymbolProfile.setSymbol(symbol.toUpperCase());
		configSymbolProfile.setProfileKey(key);
		configSymbolProfile.setProfileValue(value);
		configSymbolProfileService.createOrUpdateConfigSymbolProfile(configSymbolProfile);
	}

	// 已有交易对列表查询
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/configsymbol-list", method = RequestMethod.GET)
	public List<ConfigSymbol> getConfigSymbolList(@AuthForHeader AuthContext context) {
		List<ConfigSymbol> list = configSymbolService.getAllSymbol();
		list.stream().forEach(c -> c.setTitle(c.getAssetCode2() + "/" + c.getAssetCode1()));
		return list;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/configsymbolprofile-list", method = RequestMethod.GET)
	public List<ConfigSymbolProfile> getConfigSymbolProfileList(@AuthForHeader AuthContext context) {
		List<ConfigSymbolProfile> list = configSymbolProfileService.selectAll();
		return list;
	}

}
