package com.gop.job;

import com.google.common.collect.Maps;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.config.JobConfig;
import com.gop.config.MailConfig;
import com.gop.domain.User;
import com.gop.domain.UserBeginingBalance;
import com.gop.domain.enums.RoleStatus;
import com.gop.finance.dto.AssetDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.UserStatisticsService;
import com.gop.match.service.TradeRecordService;
import com.gop.sms.service.EmailLogService;
import com.gop.sms.service.IEmailService;
import com.gop.user.service.UserService;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@Component("userAssetStatusJob")
@EnableConfigurationProperties(MailConfig.class)
public class UserAssetStatusJob implements SimpleJob {
  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private BeginingBalanceService beginingBalanceService;
  @Autowired
  private ExportExcelService exportExcelService;
  @Autowired
  private UserService userService;
  @Autowired
  private FinanceService financeService;

  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    String STATUS_USRE_PATH_NAME = mailConfig.getReportRoot()+ "userAsset" + fileDate + ".xls";

    List<User> users = userService.getUsers(null);
    Map<Integer,String> maps = Maps.newHashMap();
    for(User user : users){
      maps.put(user.getUid(),user.getEmail());
    }
    List<AssetDto> assetDtos = financeService.getUserAccountList(null, null).stream().map(finance -> new AssetDto(
        finance,
        maps.get(finance.getUid())
    )).collect(Collectors.toList());
    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
    for(AssetDto assetDto : assetDtos){
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("UID",assetDto.getUserId());
      resMap.put("email",assetDto.getAccountNo());
      resMap.put("assetCode",assetDto.getAssetCode());
      resMap.put("amountAvailable",assetDto.getAmountAvailable());
      resMap.put("amountLock",assetDto.getAmountLock());
      resMap.put("amountTotal",assetDto.getAmountTotal());
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName("userAsset.ftl",resultMerStlMap, STATUS_USRE_PATH_NAME);

    //将数据存储到数据库中
    try {
      List<UserBeginingBalance> userBeginingBalances = assetDtos.stream().map(assetDto -> {
        UserBeginingBalance balance = new UserBeginingBalance();
        balance.setUid(assetDto.getUserId());
        balance.setAssetCode(assetDto.getAssetCode());
        balance.setAccountNo(assetDto.getAccountNo());
        balance.setAmount_available(assetDto.getAmountAvailable());
        balance.setAmount_lock(assetDto.getAmountLock());
        balance.setAmount_total(assetDto.getAmountTotal());
        balance.setStatus(RoleStatus.ENABLE.getStatus());
        balance.setCreateDate(new Date());
        return balance;
      }).collect(Collectors.toList());
      if(beginingBalanceService.countUserBeginBalance(new Date())> 0){
        beginingBalanceService.updateUserBeginBalanceStatus(new Date());
      }
      beginingBalanceService.insertUserBatch(userBeginingBalances);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
