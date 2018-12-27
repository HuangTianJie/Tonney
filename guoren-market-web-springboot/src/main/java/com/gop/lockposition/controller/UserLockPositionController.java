package com.gop.lockposition.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.LockPositionCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.domain.Finance;
import com.gop.domain.UserLockPositionOperDetail;
import com.gop.domain.UserLockPositionReward;
import com.gop.exception.AppException;
import com.gop.lock.position.service.IUserLockService;
import com.gop.lock.position.service.dto.UserLockPositionBasicDto;
import com.gop.mode.vo.PageModel;
import com.gop.util.BigDecimalUtils;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("UserLockPositionController")	
@RequestMapping("/lockposition")
public class UserLockPositionController {

	@Autowired
	private IUserLockService iUserLockService;
	
	@Autowired
	private ConfigAssetService configAssetService;
	
	@Autowired
	private FinanceService financeService;

	@RequestMapping(value = "/lock", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	public void lockPosition(@AuthForHeader AuthContext context,@RequestParam("assetCode") String assetCode,@RequestParam("amount") BigDecimal amount) {
		Integer uid = context.getLoginSession().getUserId();
		//校验时间是否在可锁仓时间范围之内
		UserLockPositionBasicDto userLockPositionBasicDto = iUserLockService.getUserLockInfo(uid, assetCode);
		if(new Date().getTime() <= userLockPositionBasicDto.getBeginDate().getTime() || new Date().getTime() >= userLockPositionBasicDto.getEndDate().getTime()) {
			throw new AppException(LockPositionCodeConst.CURRENT_TIME_CAN_NOT_LOCK);
		}
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		//零值校验
		if(BigDecimalUtils.isLessOrEqual(amount, BigDecimal.ZERO)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		Finance finance = financeService.queryAccount(uid, assetCode);
		//空指针异常校验
		if(finance == null) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		//校验锁仓个数是否超过资产余额
		if(BigDecimalUtils.isBigger(amount, finance.getAmountAvailable())) {
			throw new AppException(LockPositionCodeConst.AVAILABLE_ACOUNT_LESS);
		}
		iUserLockService.userLockPosition(uid, assetCode, amount);
	}

	@RequestMapping(value = "/unlock", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	public void unLock(@AuthForHeader AuthContext context, @RequestParam("assetCode") String assetCode) {
		Integer uid = context.getLoginSession().getUserId();
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		iUserLockService.userUnLockPosition(uid, assetCode);
	}
	
	@RequestMapping(value = "/lock-info", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public UserLockPositionBasicDto lockInfo(@AuthForHeader AuthContext context, @RequestParam("assetCode") String assetCode) {
		Integer uid = context.getLoginSession().getUserId();
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		return iUserLockService.getUserLockInfo(uid, assetCode);
	}
	
	@RequestMapping(value = "/lock-list", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<UserLockPositionOperDetail> lockList(@AuthForHeader AuthContext context, @RequestParam("assetCode") String assetCode,
			@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
		Integer uid = context.getLoginSession().getUserId();
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		return iUserLockService.getUserOperationList(uid,assetCode, pageNo, pageSize);
		 
	}
	
	@RequestMapping(value = "/lock-reward-query", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<UserLockPositionReward> lockRewardQuery(@AuthForHeader AuthContext context, @RequestParam("assetCode") String assetCode,
			@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
		// 币种校验
		List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
		Set<String> assetCodes = configAssets.stream().map(a -> a.getAssetCode()).collect(Collectors.toSet());
		if (!(assetCodes.contains(assetCode))) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		return iUserLockService.getRewardList(assetCode, pageNo, pageSize);
	}
	
}
