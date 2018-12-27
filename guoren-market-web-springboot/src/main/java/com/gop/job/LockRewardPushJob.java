package com.gop.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.domain.UserLockPositionConfigType;
import com.gop.domain.UserLockPositionReward;
import com.gop.domain.enums.UserLockPositionRewardStatus;
import com.gop.domain.enums.UserMessageCategory;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.lock.position.service.IUserLockPositionConfigService;
import com.gop.lock.position.service.IUserLockPositionRewardService;
import com.gop.user.service.UserMessageService;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-22 下午6:32 每月锁仓奖励发放定时任务
 */
@Slf4j
@Component("lockRewardPushJob")
public class LockRewardPushJob implements SimpleJob {

	@Autowired
	private IUserLockPositionRewardService userLockPositionRewardService;
	@Autowired
	private UserMessageService userMessageService;
	@Autowired
	private UserAccountFacade userAccountFacade;
	@Autowired
	private IUserLockPositionConfigService userLockPositionConfigService;

	@Override
	@Transactional
	public void execute(ShardingContext shardingContext) {
		long currentTime = System.currentTimeMillis();
		log.info("LockRewardPushJob|begin|time={}",
				DateFormatUtils.format(new Date(currentTime), "yyyy-MM-dd HH:mm:ss"));

		List<UserLockPositionReward> unPushList = userLockPositionRewardService
				.getRewardByStatus(UserLockPositionRewardStatus.UNGRANT);
		unPushList.forEach(reward -> {
			String foundationUid = userLockPositionConfigService.getConfigByAssetCodeAndKey(reward.getAssetCode(),
					UserLockPositionConfigType.FOUNDATIONUID).getProfileValue();
			String coinType = reward.getAssetCode();
			BigDecimal rewardAmount = reward.getRewardAmount();
			// Finance方法处理奖励
			List<AssetOperationDto> assetOperationDtos = new ArrayList<AssetOperationDto>();
			// 获奖用户dto
			AssetOperationDto assetOperationRewardDto = new AssetOperationDto();
			assetOperationRewardDto.setUid(reward.getUid());
			assetOperationRewardDto.setRequestNo(reward.getRequestNo());
			assetOperationRewardDto.setBusinessSubject(BusinessSubject.SYSTEM_TRANSFER);
			assetOperationRewardDto.setAssetCode(reward.getAssetCode());
			assetOperationRewardDto.setAmount(reward.getRewardAmount());
			assetOperationRewardDto.setLockAmount(BigDecimal.ZERO);
			assetOperationRewardDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationRewardDto.setAccountClass(AccountClass.ASSET);
			assetOperationRewardDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			assetOperationRewardDto.setIndex(0);
			assetOperationDtos.add(assetOperationRewardDto);
			// 基金dto
			AssetOperationDto assetOperationFundDto = new AssetOperationDto();
			assetOperationFundDto.setUid(Integer.valueOf(foundationUid));
			assetOperationFundDto.setRequestNo(reward.getRequestNo());
			assetOperationFundDto.setBusinessSubject(BusinessSubject.SYSTEM_TRANSFER);
			assetOperationFundDto.setAssetCode(reward.getAssetCode());
			assetOperationFundDto.setAmount(reward.getRewardAmount().negate());
			assetOperationFundDto.setLockAmount(BigDecimal.ZERO);
			assetOperationFundDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationFundDto.setAccountClass(AccountClass.ASSET);
			assetOperationFundDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			assetOperationFundDto.setIndex(1);
			assetOperationDtos.add(assetOperationFundDto);
			// 获奖用户获得数字资产,基金账户扣除资产
			userAccountFacade.assetOperation(assetOperationDtos);
			reward.setStatus(UserLockPositionRewardStatus.GRANTED);
			reward.setUpdateDate(new Date(currentTime));
			userLockPositionRewardService.updateByPrimaryKeySelective(reward);
			// TODO message待填
			String message = null;
			userMessageService.insertMessage(reward.getUid(), message, UserMessageCategory.ASSETS);
		});
		log.info("LockRewardPushJob|normalEnd|cost {} ms", System.currentTimeMillis() - currentTime);

	}
}
