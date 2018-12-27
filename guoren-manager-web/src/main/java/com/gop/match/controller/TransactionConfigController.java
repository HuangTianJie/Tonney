package com.gop.match.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MatchCodeConst;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.ConfigSymbolProfile;
import com.gop.domain.enums.ConfigSymbolType;
import com.gop.domain.enums.SymbolStatus;
import com.gop.exception.AppException;
import com.gop.match.dto.ConfigSymbolDto;
import com.gop.match.service.ConfigSymbolProfileService;
import com.gop.match.service.ConfigSymbolService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

/**
 * 交易对收费设置
 * 
 * @author caleb
 *
 */
@RestController("TransactionConfigController")
@RequestMapping("/transaction-config")
@Slf4j
public class TransactionConfigController {
	@Autowired
	private ConfigSymbolProfileService configSymbolProfileService;
	@Autowired
	private ConfigSymbolService configSymbolService;

	//
	@ApiOperation(value="更新交易对收费")
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/config-update", method = RequestMethod.GET)
	public void coinTradeFeeConfigUpdate(@AuthForHeader AuthContext context,
			@RequestParam("configSymbol") @ApiParam(defaultValue = "ETH_MFT") String symbol,
			@RequestParam("key") ConfigSymbolType key,
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
		if (SymbolStatus.DELISTED.equals(configSymbol.getStatus())) {
			throw new AppException(MatchCodeConst.SYMBOL_CLOSED);
		}
		ConfigSymbolProfile configSymbolProfile = new ConfigSymbolProfile();
		configSymbolProfile.setSymbol(symbol.toUpperCase());
		configSymbolProfile.setProfileKey(key);
		configSymbolProfile.setProfileValue(value);
		configSymbolProfileService.createOrUpdateConfigSymbolProfile(configSymbolProfile);
	}

	// 查询交易对费率列表
	// @Strategys(strategys = { @Strategy(authStrategy =
	// "exe({{'checkLoginStrategy'}})") })
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})") })
	@RequestMapping(value = "/config-list", method = RequestMethod.GET)
	// 参数加string symbol, configSymbol
	public List<ConfigSymbolDto> coinTradeFeeConfigList(@AuthForHeader AuthContext context,
			@RequestParam(value = "key") ConfigSymbolType key) {
		// 查询列表

		List<ConfigSymbolProfile> list = configSymbolProfileService.getConfigSymbolProfileByKey(key);
		List<ConfigSymbolDto> result = new ArrayList<>();
		// 如果非空,则迭代赋值给dto传给前端
		// 查询抛异常添加在service的impl中
		if (null != list) {
			for (ConfigSymbolProfile profile : list) {
				ConfigSymbolDto dto = new ConfigSymbolDto();
				dto.setId(profile.getId());
				dto.setSymbol(profile.getSymbol());
				dto.setProfileKey(profile.getProfileKey());
				dto.setProfileValue(profile.getProfileValue());
				result.add(dto);
			}
		} else {
			// 如果为空,则将传给前端的list赋值为空
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

}
