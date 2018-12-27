package com.gop.lockposition.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.domain.User;
import com.gop.domain.UserLockPositionConfig;
import com.gop.domain.UserLockPositionConfigType;
import com.gop.domain.UserLockPositionOperDetail;
import com.gop.domain.UserLockPositionTotalReward;
import com.gop.domain.enums.AssetStatus;
import com.gop.exception.AppException;
import com.gop.lock.position.service.IUserLockPositionConfigService;
import com.gop.lock.position.service.IUserLockPositionTotalRewardService;
import com.gop.lock.position.service.IUserLockService;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("UserLockPositionController")
@RequestMapping("/lockposition")
public class UserLockPositionController {
	@Autowired
	private IUserLockPositionConfigService userLockPositionConfigService;
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private IUserLockService userLockService;
	@Autowired
	private IUserLockPositionTotalRewardService userLockPositionTotalRewardService;
	@Autowired
	private IUserLockService iUserLockService;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/lock-config", method = RequestMethod.GET)
	public void lockConfig(@AuthForHeader AuthContext context, @RequestParam("assetCode") String assetCode,
			@RequestParam("key") UserLockPositionConfigType key, @RequestParam("value") String value) {
		if (!configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (!key.validateValue(value)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (UserLockPositionConfigType.FOUNDATIONUID.equals(key)
				&& (null == userFacade.getUser(Integer.valueOf(value)))) {
			throw new AppException(UserCodeConst.NO_REGISTER);
		}
		UserLockPositionConfig config = new UserLockPositionConfig();
		config.setAssetCode(assetCode);
		config.setProfileKey(key);
		config.setProfileValue(value);
		userLockPositionConfigService.createOrUpdateConfig(config);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})") })
	@RequestMapping(value = "/lock-list", method = RequestMethod.GET)
	public PageModel<UserLockPositionConfig> lockList(@AuthForHeader AuthContext context,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		PageModel<UserLockPositionConfig> model = userLockPositionConfigService.getConfigList(assetCode, pageNo,
				pageSize);
		return model;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/lock-user-record-list", method = RequestMethod.GET)
	public PageModel<UserLockPositionOperDetail> lockUserRecordList(@AuthForHeader AuthContext context,
			@RequestParam(value = "uid", required = false) Integer uid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		// 币种校验
		if (!Strings.isNullOrEmpty(assetCode)) {
			List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
			Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
			if (!(assetCodes.contains(assetCode))) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		return userLockService.getUserOperationList(uid, assetCode, pageNo, pageSize);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/lock-total-reward-list", method = RequestMethod.GET)
	public PageModel<UserLockPositionTotalReward> lockTotalRewardList(@AuthForHeader AuthContext context,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		// 币种校验
		if (!Strings.isNullOrEmpty(assetCode)) {
			List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
			Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
			if (!(assetCodes.contains(assetCode))) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		return userLockPositionTotalRewardService.getTotalRewardList(assetCode, pageNo, pageSize);
	}
	
	//清算锁仓奖励
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/lock-calculate-reward", method = RequestMethod.GET)
	public void lockCalculateReward(@AuthForHeader AuthContext context,@RequestParam("assetCode") String assetCode,
			@RequestParam("rewardYear") Integer rewardYear,@RequestParam("rewardMonth") Integer rewardMonth) {
		// 币种校验
		if (!Strings.isNullOrEmpty(assetCode)) {
			List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
			Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
			if (!(assetCodes.contains(assetCode))) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		userLockService.calculateUserLockPositionReward(assetCode, rewardYear, rewardMonth);
	}
	
	//发放锁仓奖励
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/lock-grant-reward", method = RequestMethod.GET)
	public void lockGrantReward(@AuthForHeader AuthContext context,@RequestParam("assetCode") String assetCode,
			@RequestParam("rewardYear") Integer rewardYear,@RequestParam("rewardMonth") Integer rewardMonth) {
		// 币种校验
		if (!Strings.isNullOrEmpty(assetCode)) {
			List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
			Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
			if (!(assetCodes.contains(assetCode))) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		userLockService.grantUserLockPositionReward(assetCode, rewardYear, rewardMonth);
	}
	
	//强制解锁用户锁仓
	@RequestMapping(value = "/force-unlock", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	public void unLock(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode) {
		//查询用户是否存在
		User user = userFacade.getUser(uid);
		if (null == user) {
			throw new AppException(UserAssetCodeConst.USER_ACCOUNT_NOT_EXIST);
		}
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		iUserLockService.userUnLockPosition(uid, assetCode);
	}
}
