package com.gop.lock.position.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.LockPositionCodeConst;
import com.gop.domain.User;
import com.gop.domain.UserLockPosition;
import com.gop.domain.UserLockPositionConfigType;
import com.gop.domain.UserLockPositionGrantRewardDetail;
import com.gop.domain.UserLockPositionOperDetail;
import com.gop.domain.UserLockPositionOperRecord;
import com.gop.domain.UserLockPositionReward;
import com.gop.domain.UserLockPositionTotalReward;
import com.gop.domain.enums.UserLockPositionRewardStatus;
import com.gop.domain.enums.UserLockPositionStatus;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.lock.position.service.IUserLockPositionConfigService;
import com.gop.lock.position.service.IUserLockPositionGrantRewardDetailService;
import com.gop.lock.position.service.IUserLockPositionOperDetailService;
import com.gop.lock.position.service.IUserLockPositionOperRecordService;
import com.gop.lock.position.service.IUserLockPositionRewardService;
import com.gop.lock.position.service.IUserLockPositionService;
import com.gop.lock.position.service.IUserLockPositionTotalRewardService;
import com.gop.lock.position.service.IUserLockService;
import com.gop.lock.position.service.dto.UserLockPositionBasicDto;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.util.BigDecimalUtils;
import com.gop.util.SequenceUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-22 下午3:34
 */
@Service
@Slf4j
public class UserLockServiceImpl implements IUserLockService {

	@Autowired
	private IUserLockPositionRewardService userLockPositionRewardService;
	@Autowired
	private IUserLockPositionOperRecordService userLockPositionOperRecordService;
	@Autowired
	private IUserLockPositionOperDetailService userLockPositionOperDetailService;
	@Autowired
	private UserAccountFacade userAccountFacade;
	@Autowired
	private IUserLockPositionService userLockPositionService;
	@Autowired
	private IUserLockPositionConfigService userLockPositionConfigService;
	@Autowired
	private IUserLockPositionTotalRewardService userLockPositionTotalRewardService;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private IUserLockPositionGrantRewardDetailService userLockPositionGrantRewardDetailService;
	 
	private static String goopalEmailSuffix= "@goopal.com";
	
	private static String new4gEmailSuffix = "@new4g.cn";
	
	private static String qqEmailSuffix = "@qq.com";

	@Override
	public UserLockPositionBasicDto getUserLockInfo(Integer userId, String coinType) {
		UserAccountDto userAccountDto = userAccountFacade.queryAccount(userId, coinType);
		BigDecimal amountAvailable = userAccountDto.getAmountAvailable();
		List<UserLockPositionOperRecord> positionOperRecords = userLockPositionOperRecordService.selectByUserId(userId);

		BigDecimal lockAmount = positionOperRecords.stream().filter(
				userLockPositionOperRecord -> userLockPositionOperRecord.getStatus() == UserLockPositionStatus.LOCK)
				.map(UserLockPositionOperRecord::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

		UserLockPositionReward params = UserLockPositionReward.builder().assetCode(coinType).uid(userId)
				.createDate(getCurrentMonthFirstDay()).build();
		UserLockPositionReward userLockPositionReward = userLockPositionRewardService.selectByUserIdAndTime(params);
		BigDecimal lastRewardAmount = BigDecimal.ZERO;
		if (Objects.nonNull(userLockPositionReward)) {
			lastRewardAmount = userLockPositionReward.getRewardAmount();
		}
		BigDecimal totalReward = userLockPositionRewardService.getTotalRewardByUserId(userId, coinType);
		UserLockPositionBasicDto dto = UserLockPositionBasicDto.builder()
								       .uid(userId)
								       .coinType(coinType)
								       .lockAmount(lockAmount)
								       .amount(amountAvailable)
								       .totalRewardAmount(totalReward)
								       .lastRewardAmount(lastRewardAmount)
								       .build();
		return calDate(dto);
	}

	@Override
	public PageModel<UserLockPositionReward> getRewardList(String coinType, Integer page, Integer pageSize) {
		PageModel<UserLockPositionReward> pageModel = new PageModel<>();
		Integer totalRecords = userLockPositionRewardService.countByCoinType(coinType,
				UserLockPositionRewardStatus.GRANTED);
		if (noRecords(page, pageSize, pageModel, totalRecords)) {
			return pageModel;
		}
		Integer start = (page - 1) * pageSize;
		List<UserLockPositionReward> rewardList = userLockPositionRewardService.getRewardList(coinType, start, pageSize,
				UserLockPositionRewardStatus.GRANTED);
		Integer totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize : totalRecords / pageSize + 1;

		pageModel.setPageNo(page);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(totalPage);
		pageModel.setTotal(Long.parseLong(totalRecords.toString()));
		pageModel.setList(rewardList);
		return pageModel;
	}

	@Override
	public PageModel<UserLockPositionOperDetail> getUserOperationList(Integer userId,String assetCode, Integer page, Integer pageSize) {
		PageModel<UserLockPositionOperDetail> pageModel = new PageModel<>();
		Integer totalRecords = userLockPositionOperDetailService.countUserOperation(userId);
		if (null != userId && totalRecords == 0) {
			pageModel.setPageNo(page);
			pageModel.setPageSize(pageSize);
			pageModel.setPageNum(0);
			pageModel.setTotal(0L);
			return pageModel;
		}
		Integer start = (page - 1) * pageSize;
		List<UserLockPositionOperDetail> userOperationList = userLockPositionOperDetailService
				.getUserOperationList(userId,assetCode, start, pageSize);
		Integer totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize : totalRecords / pageSize + 1;

		pageModel.setPageNo(page);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(totalPage);
		pageModel.setTotal(Long.parseLong(totalRecords.toString()));
		pageModel.setList(userOperationList);
		return pageModel;
	}

	private boolean noRecords(Integer page, Integer pageSize, PageModel<UserLockPositionReward> pageModel,
			Integer totalRecords) {
		if (totalRecords == 0) {
			pageModel.setPageNo(page);
			pageModel.setPageSize(pageSize);
			pageModel.setPageNum(0);
			pageModel.setTotal(0L);
			return true;
		}
		return false;
	}

	/**
	 * 获取当前月的第一天
	 */
	private Date getCurrentMonthFirstDay() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 计算本月开始结束时间
	 */
	private UserLockPositionBasicDto calDate(UserLockPositionBasicDto userLockPositionBasicDto) {
		Integer day = Integer.valueOf(userLockPositionConfigService
				.getConfigByAssetCodeAndKey(userLockPositionBasicDto.getCoinType(), UserLockPositionConfigType.LOCKDAY)
				.getProfileValue());
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		Date endTime = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 0 - day + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		Date beginTime = calendar.getTime();
		userLockPositionBasicDto.setBeginDate(beginTime);
		userLockPositionBasicDto.setEndDate(endTime);
		return userLockPositionBasicDto;
	}

	@Override
	@Transactional
	public void userLockPosition(Integer uid, String assetCode, BigDecimal amount) {
		try {
			String requestNo = SequenceUtil.getNextId();
			// 1.锁仓记录表新增一条数据
			UserLockPositionOperRecord userLockPositionOperRecord = new UserLockPositionOperRecord();
			userLockPositionOperRecord.setUid(uid);
			userLockPositionOperRecord.setAssetCode(assetCode);
			userLockPositionOperRecord.setAmount(amount);
			userLockPositionOperRecord.setStatus(UserLockPositionStatus.LOCK);
			userLockPositionOperRecordService.addUserLockPositionOperRecord(userLockPositionOperRecord);
			// 2.查询锁仓统计表
			UserLockPosition userLockPosition = userLockPositionService.selectByUidAndAssetCode(uid, assetCode);
			// 3.锁仓统计表插入或更新数据
			Integer operId = 0;
			BigDecimal totalLockAmount = BigDecimal.ZERO;
			if (null == userLockPosition) {
				UserLockPosition newUserLockPosition = new UserLockPosition();
				newUserLockPosition.setUid(uid);
				newUserLockPosition.setAssetCode(assetCode);
				newUserLockPosition.setAmount(amount);
				userLockPositionService.addUserLockPosition(newUserLockPosition);
				operId = newUserLockPosition.getId();
				totalLockAmount = amount;
			} else {
				userLockPosition.setAmount(userLockPosition.getAmount().add(amount));
				userLockPositionService.updateUserLockPositionAmountByUidAndAssetCode(userLockPosition);
				operId = userLockPosition.getId();
				totalLockAmount = userLockPosition.getAmount();
			}
			// 4.锁仓记录流水表新增一条数据
			UserLockPositionOperDetail userLockPositionOperDetail = new UserLockPositionOperDetail();
			userLockPositionOperDetail.setOperId(operId);
			userLockPositionOperDetail.setUid(uid);
			userLockPositionOperDetail.setAssetCode(assetCode);
			userLockPositionOperDetail.setTotalLockAmount(totalLockAmount);
			userLockPositionOperDetail.setLockAmount(amount);
			userLockPositionOperDetail.setStatus(UserLockPositionStatus.LOCK);
			userLockPositionOperDetail.setRequestNo(requestNo);
			userLockPositionOperDetailService.addUserLockPositionOperDetail(userLockPositionOperDetail);
			// 5.给用户冻结锁仓资产
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(requestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.LOCK);
			assetOperationDto.setAssetCode(assetCode);
			assetOperationDto.setAmount(amount.negate());
			assetOperationDto.setLockAmount(amount);
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			assetOperationDto.setIndex(0);
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList(assetOperationDto);
			userAccountFacade.assetOperation(assetOperationDtos);

		} catch (Exception e) {
			log.error("用户锁仓异常 userId={},eMessage=" + e.getMessage(), uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	@Transactional
	public void userUnLockPosition(Integer uid, String assetCode) {
		try {
			String requestNo = SequenceUtil.getNextId();
			// 1.查询锁仓统计表
			UserLockPosition userLockPosition = userLockPositionService.selectByUidAndAssetCode(uid, assetCode);
			// 2.更新用户锁仓统计表用户金额为0
			if (null == userLockPosition) {
				throw new AppException(LockPositionCodeConst.NO_ACOUNT_UNLOCK_ERROR);
			}
			if (BigDecimalUtils.isEqualZero(userLockPosition.getAmount())) {
				throw new AppException(LockPositionCodeConst.NO_ACOUNT_UNLOCK_ERROR);
			}
			userLockPositionService.updateUserLockPositionAmountByid(userLockPosition.getId(), BigDecimal.ZERO);
			// 3.将该用户的锁仓记录状态更新为已解锁
			userLockPositionOperRecordService.updateLockPositionOperRecordStatusByUidAndAssetCode(uid,assetCode,
					UserLockPositionStatus.LOCK,UserLockPositionStatus.UNLOCK);
			// 4.锁仓记录表新增一条解锁记录
			UserLockPositionOperRecord userLockPositionOperRecord = new UserLockPositionOperRecord();
			userLockPositionOperRecord.setUid(uid);
			userLockPositionOperRecord.setAssetCode(assetCode);
			userLockPositionOperRecord.setAmount(userLockPosition.getAmount().negate());
			userLockPositionOperRecord.setStatus(UserLockPositionStatus.UNLOCK);
			userLockPositionOperRecordService.addUserLockPositionOperRecord(userLockPositionOperRecord);
			// 4.锁仓记录流水表新增一条数据
			UserLockPositionOperDetail userLockPositionOperDetail = new UserLockPositionOperDetail();
			userLockPositionOperDetail.setOperId(userLockPositionOperRecord.getId());
			userLockPositionOperDetail.setUid(uid);
			userLockPositionOperDetail.setAssetCode(assetCode);
			userLockPositionOperDetail.setTotalLockAmount(BigDecimal.ZERO);
			userLockPositionOperDetail.setLockAmount(userLockPosition.getAmount().negate());
			userLockPositionOperDetail.setStatus(UserLockPositionStatus.UNLOCK);
			userLockPositionOperDetail.setRequestNo(requestNo);
			userLockPositionOperDetailService.addUserLockPositionOperDetail(userLockPositionOperDetail);
			// 5.给用户解冻锁仓资产
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(requestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.UNLOCK);
			assetOperationDto.setAssetCode(assetCode);
			assetOperationDto.setAmount(userLockPosition.getAmount());
			assetOperationDto.setLockAmount(userLockPosition.getAmount().negate());
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			assetOperationDto.setIndex(0);
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList(assetOperationDto);
			userAccountFacade.assetOperation(assetOperationDtos);

		} catch (Exception e) {
			log.error("用户解锁仓异常 userId={},eMessage=" + e.getMessage(), uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

	}

	@Override
	@Transactional
	public void calculateUserLockPositionReward(String assetCode, Integer rewardYear, Integer rewardMonth) {
		try {
			//校验当前时间不能小于要清算月份的最后一天 
			Date lastMonthLastDay = getLastMonthLastDay(rewardYear,rewardMonth);
			if(new Date().getTime() <= lastMonthLastDay.getTime()) {
				throw new AppException(LockPositionCodeConst.CURRENT_TIME_CAN_NOT_CALCULATE);
			}
			//1.查询该币种用户锁仓情况汇总
	        List<UserLockPositionOperRecord> recordList = getRewardsGroupByUser(assetCode,rewardYear,rewardMonth);
	        //2.统计该币种的锁定总量
	        BigDecimal totalLockAmount = BigDecimal.ZERO;
	        for (UserLockPositionOperRecord record : recordList) {
	        	totalLockAmount = totalLockAmount.add(record.getAmount());
	        }
	        //3.算出该币种比例发放率 ：总奖励/总锁定量
    		Integer foundationUid = Integer.valueOf(userLockPositionConfigService.getConfigByAssetCodeAndKey(assetCode, UserLockPositionConfigType.FOUNDATIONUID).getProfileValue());
    		BigDecimal balance = userAccountFacade.queryAccount(foundationUid, assetCode).getAmountAvailable();
    		//4.总奖励记录表添加一条记录
    		UserLockPositionTotalReward totalReward = new UserLockPositionTotalReward();
    		totalReward.setAssetCode(assetCode);
    		totalReward.setRewardYear(rewardYear);
    		totalReward.setRewardMonth(rewardMonth);
    		totalReward.setTotalRewardAmount(balance);
    		totalReward.setTotalLockAmount(totalLockAmount);
    		userLockPositionTotalRewardService.insertSelective(totalReward);
	        BigDecimal rate = balance.divide(totalLockAmount,5,BigDecimal.ROUND_DOWN);
	        //5.奖励记录表添加清算奖励记录
	        List<UserLockPositionReward> rewardList = new ArrayList<>();
	        recordList.forEach(record -> {
	            String encodeEmail = getEncodeEmail(record.getUid());
	            BigDecimal rewardAmount = record.getAmount().multiply(rate);
	            UserLockPositionReward userLockPositionReward = new UserLockPositionReward();
	            userLockPositionReward.setAssetCode(record.getAssetCode());
	            userLockPositionReward.setUid(record.getUid());
	            userLockPositionReward.setEmail(encodeEmail);
	            userLockPositionReward.setRewardAmount(rewardAmount);
	            userLockPositionReward.setLockAmount(record.getAmount());
	            userLockPositionReward.setStatus(UserLockPositionRewardStatus.UNGRANT);
	            userLockPositionReward.setRewardYear(rewardYear);
	            userLockPositionReward.setRewardMonth(rewardMonth);
	            String requestNo = SequenceUtil.getNextId();
	            userLockPositionReward.setRequestNo(requestNo);
	            rewardList.add(userLockPositionReward);
	        });
	        rewardList.forEach(userLockPositionRewardService::insertSelective);
		} catch (Exception e) {
			log.error("清算奖励异常 assetCode={},eMessage=" + e.getMessage(), assetCode, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}
	
	private List<UserLockPositionOperRecord> getRewardsGroupByUser(String assetCode,Integer rewardYear,Integer rewardMonth) {
        Date lastMonthFirstDay = getLastMonthFirstDay(rewardYear,rewardMonth);
        Date lastMonthLastDay = getLastMonthLastDay(rewardYear,rewardMonth);
        UserLockPositionOperRecord params = new UserLockPositionOperRecord();
        params.setAssetCode(assetCode);
        params.setCreateDate(lastMonthFirstDay);
        params.setUpdateDate(lastMonthLastDay);
        List<UserLockPositionOperRecord> rewardList =
            userLockPositionOperRecordService.getLastMonthRewardList(params);
        List<UserLockPositionOperRecord> resultList = Lists.newArrayList();
        
        //合并同一个用户的多条记录
        Map<Integer, UserLockPositionOperRecord> map = new HashMap<>();
        rewardList.forEach(record -> {
            Integer uid = record.getUid();
            if (map.containsKey(uid)) {
                record.setAmount(record.getAmount().add(map.get(uid).getAmount()));
            }
            map.put(uid, record);
        });

        List<UserLockPositionOperRecord> tempList =
            map.keySet().stream().map(map::get).collect(Collectors.toList());
        resultList.addAll(tempList);
        return resultList;
    }

    /**
     * 获取加密的邮箱
     */
    private String getEncodeEmail(Integer userId) {
        User user = userFacade.getUser(userId);
        String[] split = user.getEmail().split("@");
        String emailName = split[0];
        int length = emailName.length();
        String result;
        if (length == 1) {
            result = Joiner.on("@").join(emailName + "**" + emailName, split[1]);
        } else {
            String start = emailName.substring(0, 1);
            String end = emailName.substring(length - 1, length);
            result = Joiner.on("@").join(start + "**" + end, split[1]);
        }
        result = result.replace(goopalEmailSuffix, qqEmailSuffix);
        result = result.replace(new4gEmailSuffix, qqEmailSuffix);
        return result;
    }

    /**
     * 获取清算奖励月的第一天的时间
     */
    private Date getLastMonthFirstDay(Integer rewardYear,Integer rewardMonth) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        calendar.set(Calendar.YEAR,rewardYear);
        calendar.set(Calendar.MONTH, rewardMonth -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND,000);
        return new Date(calendar.getTimeInMillis());
    }
    
    /**
     * 获取清算奖励月的最后一天的时间
     */
    private Date getLastMonthLastDay(Integer rewardYear,Integer rewardMonth) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        calendar.set(Calendar.YEAR, rewardYear);
        calendar.set(Calendar.MONTH, rewardMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND,999);
        return new Date(calendar.getTimeInMillis());
    }

	@Override
	@Transactional
	public void grantUserLockPositionReward(String assetCode, Integer rewardYear, Integer rewardMonth) {
		try {
			//1.根据年份及月份查询清算奖励
			List<UserLockPositionReward> userLockPositionRewards = userLockPositionRewardService.getRewardByYearAndMonth(assetCode, rewardYear, rewardMonth);
			if(userLockPositionRewards.size() == 0) {
				throw new AppException(LockPositionCodeConst.NOT_CALCULATE_REWARD);
			}
			//2.校验当月奖励是否发放
			if (UserLockPositionRewardStatus.GRANTED.equals(userLockPositionRewards.get(0).getStatus())) {
				throw new AppException(LockPositionCodeConst.HAS_GRANTED_REWARD);
			}
			//3.校验当前账户金额是否够发放奖励的资金
    		Integer foundationUid = Integer.valueOf(userLockPositionConfigService.getConfigByAssetCodeAndKey(assetCode, UserLockPositionConfigType.FOUNDATIONUID).getProfileValue());
    		BigDecimal balance = userAccountFacade.queryAccount(foundationUid, assetCode).getAmountAvailable();
    		UserLockPositionTotalReward userLockPositionTotalReward = userLockPositionTotalRewardService.selectTotalRewardByAssetCodeAndYearAndMonth(assetCode, rewardYear, rewardMonth);
    		BigDecimal totalRewardAmount = userLockPositionTotalReward.getTotalRewardAmount();
    		//4.校验当前可用余额是否够用户发放奖励
    		if (BigDecimalUtils.isBigger(totalRewardAmount, balance)) {
    			throw new AppException(LockPositionCodeConst.BALANCE_CAN_NOT_GRANT_REWARD);
    		}
    		for(UserLockPositionReward reward : userLockPositionRewards) {
				//更新用户奖励表状态为已发放
				if (userLockPositionRewardService.updateRewardStatusById(reward.getId(), UserLockPositionRewardStatus.UNGRANT, UserLockPositionRewardStatus.GRANTED) != 1) {
					throw new AppException(CommonCodeConst.SERVICE_ERROR);
				} 
				//给用户发放锁仓奖励
				AssetOperationDto assetOperationDto = new AssetOperationDto();
				assetOperationDto.setUid(reward.getUid());
				assetOperationDto.setRequestNo(reward.getRequestNo());
				assetOperationDto.setBusinessSubject(BusinessSubject.LOCK_POSITION_REWARD);
				assetOperationDto.setAssetCode(reward.getAssetCode());
				assetOperationDto.setAmount(reward.getRewardAmount());
				assetOperationDto.setLockAmount(BigDecimal.ZERO);
				assetOperationDto.setLoanAmount(BigDecimal.ZERO);
				assetOperationDto.setAccountClass(AccountClass.ASSET);
				assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
				assetOperationDto.setIndex(0);
				
				String requestNo = SequenceUtil.getNextId();
				UserLockPositionGrantRewardDetail detail = new UserLockPositionGrantRewardDetail();
				detail.setRewardId(reward.getId());
				detail.setUid(foundationUid);
				detail.setAssetCode(reward.getAssetCode());
				detail.setAmount(reward.getRewardAmount().negate());
				detail.setRequestNo(requestNo);
				userLockPositionGrantRewardDetailService.addUserLockPositionGrantRewardDetail(detail);
				//给基金账户扣减用户发放的锁仓奖励
				AssetOperationDto newassetOperationDto = new AssetOperationDto();
				newassetOperationDto.setUid(foundationUid);
				newassetOperationDto.setRequestNo(requestNo);
				newassetOperationDto.setBusinessSubject(BusinessSubject.LOCK_POSITION_REWARD_LESS);
				newassetOperationDto.setAssetCode(reward.getAssetCode());
				newassetOperationDto.setAmount(reward.getRewardAmount().negate());
				newassetOperationDto.setLockAmount(BigDecimal.ZERO);
				newassetOperationDto.setLoanAmount(BigDecimal.ZERO);
				newassetOperationDto.setAccountClass(AccountClass.ASSET);
				newassetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
				newassetOperationDto.setIndex(1);
				
				List<AssetOperationDto> assetOperationDtos = Lists.newArrayList(assetOperationDto);
				assetOperationDtos.add(newassetOperationDto);
				userAccountFacade.assetOperation(assetOperationDtos);
			}
		} catch (Exception e) {
			log.error("发放奖励异常 assetCode={},eMessage=" + e.getMessage(), assetCode, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}
}