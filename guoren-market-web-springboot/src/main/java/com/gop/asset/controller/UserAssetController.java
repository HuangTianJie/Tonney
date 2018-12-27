package com.gop.asset.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetDto;
import com.gop.asset.dto.FinanceDetailDto;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.common.CoinExchangeRateService;
import com.gop.config.CommonConfig;
import com.gop.domain.ConfigAsset;
import com.gop.domain.Finance;
import com.gop.domain.FinanceDetail;
import com.gop.domain.enums.CurrencyType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value="UserAsset",description="用户资产")
@RestController
@RequestMapping("/asset")
public class UserAssetController {

	@Autowired
	private FinanceService financeService;

	@Autowired
	private ConfigAssetService configAssetService;
	
	@Autowired
	private CoinExchangeRateService coinExchangeRateService;

	// 查询用户资产列表
	@ApiOperation(value="查询用户资产列表", notes="校验用户资产并添加新资产" )
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public List<AssetDto> getUserAccountAssets(@AuthForHeader AuthContext context, @RequestParam(name = "currencyType",required = false)CurrencyType currencyType ) {
		Integer uid = context.getLoginSession().getUserId();
		// 校验用户资产并添加新资产
		Set<String> configSet = configAssetService.getAvailableAssetCode().stream().map(a -> a.getAssetCode())
				.collect(Collectors.toSet());
		List<Finance> lst = financeService.queryAccounts(uid);
		if (lst == null) {
			lst = Collections.emptyList();
		}
		Set<String> userAccountSet = lst.stream()
				.map(a -> configSet.contains(a.getAssetCode()) ? a.getAssetCode() : null).collect(Collectors.toSet());
		configSet.removeAll(userAccountSet);
		String accountKind = "MASTER";
		int brokerId = 10003;
		if (configSet.size() != 0) {
			for (String assetCode : configSet) {
				financeService.createFinanceAccount(uid, brokerId, assetCode, accountKind);
			}
		}
		List<AssetDto> collect = financeService.selectUserAccountListByCurrencyType(uid, currencyType).stream().map(finance -> new AssetDto(finance))
				.collect(Collectors.toList());
		return coinExchangeRateService.getExchangeRate(collect);
	}

	// 查询用户单个资产
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public AssetDto getUserAccountAsset(@AuthForHeader AuthContext context,
			@RequestParam("assetCode") String userAssetCode) {
		Integer uid = context.getLoginSession().getUserId();
		ConfigAsset asset = configAssetService.getAssetConfig(userAssetCode);
		if (null == asset) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		Finance account = financeService.queryAccount(uid, userAssetCode);
		if (null == account) {
			String accountKind = "MASTER";
			int brokerId = 10003;
			if (financeService.createFinanceAccount(uid, brokerId, userAssetCode, accountKind)) {
				List<AssetDto> lst = Lists.newArrayList();
				AssetDto assetDto = new AssetDto(financeService.queryAccount(uid, userAssetCode));
				return assetDto;
			}
		}
		return new AssetDto(account);
	}

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/update-user-account", method = RequestMethod.GET)
	public void updateUserAccountAssets(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		Set<String> configSet = configAssetService.getAvailableAssetCode().stream().map(a -> a.getAssetCode())
				.collect(Collectors.toSet());
		List<Finance> lst = financeService.queryAccounts(uid);
		if (lst == null) {
			lst = Collections.emptyList();
		}
		Set<String> userAccountSet = lst.stream()
				.map(a -> configSet.contains(a.getAssetCode()) ? a.getAssetCode() : null).collect(Collectors.toSet());
		configSet.removeAll(userAccountSet);
		String accountKind = "MASTER";
		int brokerId = 10003;
		if (configSet.size() != 0) {
			for (String assetCode : configSet) {
				financeService.createFinanceAccount(uid, brokerId, assetCode, accountKind);
			}
		}

	}

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/finance-detail", method = RequestMethod.GET)
	public PageModel<FinanceDetailDto> getFinancedetail(@AuthForHeader AuthContext context,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		Integer uid = context.getLoginSession().getUserId();

		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		PageModel<FinanceDetailDto> pages = new PageModel<FinanceDetailDto>();

		PageInfo<FinanceDetail> details = financeService.queryFinanceDetail(uid, null, pageSize, pageNo);
		List<FinanceDetailDto> lstDtos = details.getList().stream().map(detail -> new FinanceDetailDto(detail))
				.collect(Collectors.toList());

		pages.setPageNo(pageNo);
		pages.setPageNum(details.getPages());
		pages.setPageSize(pageSize);
		pages.setList(lstDtos);
		return pages;
	}

}
