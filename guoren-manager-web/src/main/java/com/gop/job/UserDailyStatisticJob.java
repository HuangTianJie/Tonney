package com.gop.job;

import com.google.common.collect.Maps;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.service.FinanceService;
import com.gop.authentication.service.UserIdentificationService;
import com.gop.authentication.service.UserResidenceService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.User;
import com.gop.domain.UserBeginingBalance;
import com.gop.domain.UserResidence;
import com.gop.domain.UserStatistics;
import com.gop.domain.enums.RoleStatus;
import com.gop.finance.dto.AssetDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.UserStatisticsService;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by YAO on 2018/5/29.
 */

@Slf4j
@Component("userDailyStatisticJob")
@EnableConfigurationProperties(MailConfig.class)
public class UserDailyStatisticJob implements SimpleJob {

  @Autowired
  private UserService userService;
  @Autowired
  private UserStatisticsService userStatisticsService;
  @Autowired
  private UserIdentificationService userIdentificationService;
  @Autowired
  private UserResidenceService userResidenceService;

  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    //每天凌晨跑的是前一天的数据，记录日期为前一天
    Date date = DateUtils.add(-1);
    int nativeNum = userIdentificationService.countUserLevel(date);
    int advanceNum = userResidenceService.countUserLevel(date);
    UserStatistics userStatistics = userService.getDailyStatistic(date);
    userStatistics.setNativeUser(new BigDecimal(nativeNum));
    userStatistics.setAdvancedUser(new BigDecimal(advanceNum));
    if (userStatistics.getTotalUser().compareTo(BigDecimal.ZERO) == 0) {
      userStatistics.setNativePresent(BigDecimal.ZERO);
      userStatistics.setAdvancedPresent(BigDecimal.ZERO);
    } else {
      userStatistics.setNativePresent(
          userStatistics.getNativeUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
      userStatistics.setAdvancedPresent(
          userStatistics.getAdvancedUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
    }
    userStatistics.setStatus(1);
    userStatistics.setCreateDate(date);
    if (userStatisticsService.countUserStatistics(date) > 0) {
      userStatisticsService.updateStatus(date);
    }
    userStatisticsService.insertStatistics(userStatistics);
  }
}
