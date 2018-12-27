package com.gop.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.google.common.base.Joiner;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.domain.User;
import com.gop.domain.UserLockPositionConfigType;
import com.gop.domain.UserLockPositionOperRecord;
import com.gop.domain.UserLockPositionReward;
import com.gop.domain.UserLockPositionTotalReward;
import com.gop.domain.enums.UserLockPositionRewardStatus;
import com.gop.domain.enums.UserLockPositionStatus;
import com.gop.lock.position.service.IUserLockPositionConfigService;
import com.gop.lock.position.service.IUserLockPositionOperRecordService;
import com.gop.lock.position.service.IUserLockPositionRewardService;
import com.gop.lock.position.service.IUserLockPositionTotalRewardService;
import com.gop.user.facade.UserFacade;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-22 下午6:32
 * 每月锁仓奖励计算定时任务
 */
@Slf4j
@Component("lockRewardCalJob")
public class LockRewardCalJob implements SimpleJob {


    @Autowired
    private IUserLockPositionOperRecordService userLockPositionOperRecordService;
    @Autowired
    private IUserLockPositionRewardService userLockPositionRewardService;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private IUserLockPositionConfigService userLockPositionConfigService;
    @Autowired
	private UserAccountFacade userAccountFacade;
    @Autowired
    private IUserLockPositionTotalRewardService userLockPositionTotalRewardService;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(ShardingContext shardingContext) {
        long currentTime = System.currentTimeMillis();
        log.info("LockRewardCalJob|begin|time={}",
                 DateFormatUtils.format(new Date(currentTime), "yyyy-MM-dd HH:mm:ss"));
        List<UserLockPositionOperRecord> recordList = getRewardsGroupByUser();
        //统计各个币种的锁定总量
        Map<String,BigDecimal> coinMap = new HashMap<>();
        recordList.forEach(record -> {
            String coinType = record.getAssetCode();
            if(coinMap.containsKey(coinType)){
                BigDecimal newAmount = coinMap.get(coinType).add(record.getAmount());
                coinMap.put(coinType,newAmount);
            }else {
                coinMap.put(coinType,record.getAmount());
            }
        });
        
        //算出每个币种的比例发放率 总奖励/总锁定量
        coinMap.keySet().forEach(coinType -> {
        		Integer foundationUid = Integer.valueOf(userLockPositionConfigService.getConfigByAssetCodeAndKey(coinType, UserLockPositionConfigType.FOUNDATIONUID).getProfileValue());
        		BigDecimal balance = userAccountFacade.queryAccount(foundationUid, coinType).getAmountAvailable();
        		//总奖励记录表添加记录
        		UserLockPositionTotalReward totalReward = new UserLockPositionTotalReward();
        		Calendar calendar = new GregorianCalendar();
        		totalReward.setAssetCode(coinType);
        		totalReward.setRewardYear(calendar.get(Calendar.YEAR));
        		totalReward.setRewardMonth(calendar.get(Calendar.MONTH)+1);
        		totalReward.setTotalRewardAmount(balance);
        		totalReward.setTotalLockAmount(coinMap.get(coinType));
        		userLockPositionTotalRewardService.insertSelective(totalReward);
            BigDecimal rate = balance.divide(coinMap.get(coinType),5,BigDecimal.ROUND_DOWN);
            coinMap.put(coinType,rate);

        });


        List<UserLockPositionReward> rewardList = new ArrayList<>();
        recordList.forEach(record -> {
            String encodeEmail = getEncodeEmail(record.getUid());
            UserLockPositionOperRecord param = new UserLockPositionOperRecord();
            param.setAssetCode(record.getAssetCode());
            param.setUid(record.getUid());
            param.setUpdateDate(new Date(currentTime));
            BigDecimal totalLockAmount = userLockPositionOperRecordService.getAllAmount(param);
            BigDecimal rewardAmount = record.getAmount().multiply(coinMap.get(record.getAssetCode()));
            UserLockPositionReward userLockPositionReward = new UserLockPositionReward();
            userLockPositionReward.setAssetCode(record.getAssetCode());
            userLockPositionReward.setUid(record.getUid());
            userLockPositionReward.setEmail(encodeEmail);
            userLockPositionReward.setRewardAmount(rewardAmount);
            userLockPositionReward.setLockAmount(totalLockAmount);
            userLockPositionReward.setStatus(UserLockPositionRewardStatus.UNGRANT);
            userLockPositionReward.setUpdateDate(new Date(currentTime));
            String requestNo = SequenceUtil.getNextId();
            userLockPositionReward.setRequestNo(requestNo);
            rewardList.add(userLockPositionReward);
        });

        rewardList.forEach(userLockPositionRewardService::insertSelective);

        log.info("LockRewardCalJob|normalEnd|cost {} ms", System.currentTimeMillis() - currentTime);

    }

    private List<UserLockPositionOperRecord> getRewardsGroupByUser() {
        Date lastMonthFirstDay = getLastMonthFirstDay();
        UserLockPositionOperRecord params = new UserLockPositionOperRecord();
        params.setUpdateDate(lastMonthFirstDay);
        params.setStatus(UserLockPositionStatus.LOCK);
        List<UserLockPositionOperRecord> rewardList =
            userLockPositionOperRecordService.getLastMonthRewardList(params);
        //按币种进行分组
        Map<String, List<UserLockPositionOperRecord>> originMap =
            rewardList.stream().collect(Collectors.groupingBy(UserLockPositionOperRecord::getAssetCode));
        List<UserLockPositionOperRecord> resultList = new ArrayList<>();

        // k为同一币种,合并同一个用户的多条记录
        originMap.keySet().forEach(k -> {
            List<UserLockPositionOperRecord> originList = originMap.get(k);
            Map<Integer, UserLockPositionOperRecord> map = new HashMap<>();
            originList.forEach(record -> {
                Integer uid = record.getUid();
                if (map.containsKey(uid)) {
                    record.setAmount(record.getAmount().add(map.get(uid).getAmount()));
                }
                map.put(uid, record);
            });

            List<UserLockPositionOperRecord> tempList =
                map.keySet().stream().map(map::get).collect(Collectors.toList());
            resultList.addAll(tempList);
        });

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
        return result;
    }


    /**
     * 获取上月的第一天的时间
     */
    private Date getLastMonthFirstDay() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return new Date(calendar.getTimeInMillis());
    }
}
