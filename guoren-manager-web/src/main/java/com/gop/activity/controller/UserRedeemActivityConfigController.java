package com.gop.activity.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.activity.service.UserRedeemActivityConfigService;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.ActivityCodeConst;
import com.gop.config.CommonConfig;
import com.gop.domain.ConfigAsset;
import com.gop.domain.UserRedeemActivityConfig;
import com.gop.domain.enums.UserRedeemActivityConfigStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("UserRedeemActivityConfigController")
@RequestMapping("/redeem-activity")
public class UserRedeemActivityConfigController {
	@Autowired
	private UserRedeemActivityConfigService userRedeemActivityConfigService;
	@Autowired
	private ConfigAssetService configAssetService;

	@RequestMapping(value = "config", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public void config(@AuthForHeader AuthContext context, @RequestParam("activityName") String activityName,
			@RequestParam("assetCode") String assetCode,
			@RequestParam(value = "beginDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
			@RequestParam(value = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
			@RequestParam("status") UserRedeemActivityConfigStatus status) {
		if (endDate.getTime() <= beginDate.getTime()) {
			throw new AppException(ActivityCodeConst.FIELD_ERROR, "開始時間不可以大於結束時間");
		}
		// 币种校验
		List<ConfigAsset> availableAssetList = configAssetService.getAvailableAssetCode();
		Set<String> availableAssetSet = availableAssetList.stream().map(c -> c.getAssetCode())
				.collect(Collectors.toSet());
		if (!availableAssetSet.contains(assetCode)) {
			throw new AppException(ActivityCodeConst.FIELD_ERROR, "无效币种");
		}
		UserRedeemActivityConfig config = new UserRedeemActivityConfig();
		config.setActivityName(activityName);
		config.setAssetCode(assetCode);
		config.setBeginDate(beginDate);
		config.setEndDate(endDate);
		config.setStatus(status);
		userRedeemActivityConfigService.createOrUpdateConfig(config);
	}

	@RequestMapping(value = "configlist", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<UserRedeemActivityConfig> configList(@AuthForHeader AuthContext context,
			@RequestParam(value = "activityName", required = false) String activityName,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "status", required = false) UserRedeemActivityConfigStatus status,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {

		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		return userRedeemActivityConfigService.queryConfigList(status, pageNo, pageSize);
	}
}
