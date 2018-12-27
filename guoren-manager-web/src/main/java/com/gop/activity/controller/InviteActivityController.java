package com.gop.activity.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.activity.service.InviteActivityConfigService;
import com.gop.activity.service.InviteActivityRewardConfigService;
import com.gop.activity.service.InviteActivityRewardGrantService;
import com.gop.activity.service.InviteUserRewardRecordService;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.InviteActivityConfig;
import com.gop.domain.InviteActivityRewardConfig;
import com.gop.domain.enums.InviteActivityConfigStatus;
import com.gop.domain.enums.InviteActivityRewardConfigInviteType;
import com.gop.domain.enums.InviteActivityRewardConfigStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("InviteActivityController")
@RequestMapping("/invite")
public class InviteActivityController {
	@Autowired
	private InviteActivityConfigService inviteActivityConfigService;
	@Autowired
	private InviteActivityRewardConfigService inviteActivityRewardConfigService;
	@Autowired
	private InviteUserRewardRecordService inviteUserRewardRecordService;
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private InviteActivityRewardGrantService inviteActivityRewardGrantService;

	// 邀请活动发奖
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/reward-grant", method = RequestMethod.GET)
	public void rewardGrant(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("activityName") String activityName) {
		inviteActivityRewardGrantService.handleInviteActivityRewardByInvitedUser(uid, activityName);
	}

	// 邀请活动设置
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/invite-config", method = RequestMethod.GET)
	public void configInvite(@AuthForHeader AuthContext context, @RequestParam("activityName") String activityName,
			@RequestParam("status") InviteActivityConfigStatus status) {
		InviteActivityConfig config = new InviteActivityConfig();
		config.setActivityName(activityName);
		config.setStatus(status);
		inviteActivityConfigService.createOrUpdate(config);
	}

	// 邀请活动设置查询
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/invite-config-query", method = RequestMethod.GET)
	public PageModel<InviteActivityConfig> configInviteQuery(@AuthForHeader AuthContext context,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize,
			@RequestParam(value = "activityName", required = false) String activityName) {
		PageModel<InviteActivityConfig> pageModel = inviteActivityConfigService.queryInviteActivityConfig(pageNo,
				pageSize, activityName);
		return pageModel;
	}

	// 邀请活动礼包设置
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/invite-reward-config", method = RequestMethod.GET)
	public void configActivityInviteReward(@AuthForHeader AuthContext context,
			@RequestParam("activityId") Integer activityId, @RequestParam("rewardAssetCode") String rewardAssetCode,
			@RequestParam("amount") BigDecimal amount,
			@RequestParam("inviteType") InviteActivityRewardConfigInviteType inviteType,
			@RequestParam("status") InviteActivityRewardConfigStatus status) {
		if (null == configAssetService.getAssetConfig(rewardAssetCode)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (null == inviteActivityConfigService.getInviteActivityConfigById(activityId)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		InviteActivityRewardConfig config = new InviteActivityRewardConfig();
		config.setActivityId(activityId);
		config.setAmount(amount);
		config.setInviteType(inviteType);
		config.setStatus(status);
		config.setRewardAssetCode(rewardAssetCode);
		inviteActivityRewardConfigService.createOrUpdate(config);
	}

	// 邀请活动礼包设置查询
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/invite-reward-config-query", method = RequestMethod.GET)
	public PageModel<InviteActivityRewardConfig> configInviteRewardQuery(@AuthForHeader AuthContext context,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize,
			@RequestParam(value = "activityId", required = false) Integer activityId) {
		PageModel<InviteActivityRewardConfig> pageModel = inviteActivityRewardConfigService
				.queryInviteRewardConfig(pageNo, pageSize, activityId);
		return pageModel;
	}
}
