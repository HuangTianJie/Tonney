package com.gop.quantfund.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.domain.QuantFundConfig;
import com.gop.domain.QuantFundConfigSymbol;
import com.gop.domain.enums.AssetStatus;
import com.gop.domain.enums.QuantFundConfigSymbolStatus;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.quantfund.QuantFundConfigService;
import com.gop.quantfund.QuantFundConfigSymbolService;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("QuantFundConfigController")
@RequestMapping("/quantfund")
public class QuantFundConfigController {
	@Autowired
	private QuantFundConfigService quantFundConfigService;
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private QuantFundConfigSymbolService quantFundConfigSymbolService;
	

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/config", method = RequestMethod.GET)
	public void config(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode,
			@RequestParam("key") QuantFundConfigType key, @RequestParam("value") String value) {
		if (!key.validateValue(value)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		// 校验基金是否有效
		if (QuantFundConfigType.TICKETASSETCODE.equals(key)
				&& (!configAssetService.validateAssetConfig(fundAssetCode, AssetStatus.LISTED))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		// 校验用户是否存在
		if ((QuantFundConfigType.FUNDUID.equals(key) || QuantFundConfigType.TICKETUID.equals(key))
				&& null == userFacade.getUser(Integer.valueOf(value))) {
			throw new AppException(UserCodeConst.NO_REGISTER, "用户不存在");
		}
		QuantFundConfig quantFundConfig = new QuantFundConfig();
		quantFundConfig.setFundAssetCode(fundAssetCode);
		quantFundConfig.setProfileKey(key);
		quantFundConfig.setProfileValue(value);
		quantFundConfigService.createOrUpdate(quantFundConfig);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/fund-symbol-config", method = RequestMethod.GET)
	public void fundAssetConfig(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode,
			@RequestParam("tradeAssetCode") String tradeAssetCode,
			@RequestParam("status") QuantFundConfigSymbolStatus status) {
		// 基金与交易币种校验
		if (null == configAssetService.getAssetConfig(fundAssetCode)
				|| null == configAssetService.getAssetConfig(tradeAssetCode)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		QuantFundConfigSymbol symbol = new QuantFundConfigSymbol();
		symbol.setFundAssetCode(fundAssetCode);
		symbol.setTradeAssetCode(tradeAssetCode);
		symbol.setFundSymbol(tradeAssetCode + "_" + fundAssetCode);
		symbol.setStatus(status);
		quantFundConfigSymbolService.createOrUpdate(symbol);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/fund-symbol-list", method = RequestMethod.GET)
	public PageModel<QuantFundConfigSymbol> fundSymbolList(@AuthForHeader AuthContext context,
			@RequestParam(value = "status", required = false) QuantFundConfigSymbolStatus status,
			@RequestParam(value = "fundSymbol", required = false) String fundSymbol,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		return quantFundConfigSymbolService.getConfigSymbolPage(status, fundSymbol, pageSize, pageNo);
		
	}
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public PageModel<QuantFundConfig> list(@AuthForHeader AuthContext context,
			@RequestParam(value = "key", required = false) QuantFundConfigType key,
			@RequestParam(value = "fundAssetCode", required = false) String fundAssetCode,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		return quantFundConfigService.getConfigPageByKey(key, fundAssetCode, pageSize, pageNo);

	}

}
