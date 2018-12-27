package com.gop.job;

import com.google.common.collect.Maps;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.service.FinanceService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.BeginingBalance;
import com.gop.domain.User;
import com.gop.domain.UserBeginingBalance;
import com.gop.domain.enums.RoleStatus;
import com.gop.finance.dto.AssetDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.user.service.UserService;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component("platAssetStatusJob")
@EnableConfigurationProperties(MailConfig.class)
public class PlatAssetStatusJob implements SimpleJob {
  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private BeginingBalanceService beginingBalanceService;
  @Autowired
  private ExportExcelService exportExcelService;
  @Autowired
  private FinanceService financeService;

  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    String STATUS_PATH_NAME = mailConfig.getReportRoot()+ "platAssetStatus" + fileDate + ".xls";

    List<FinanceAmountDto> financeAmountDtos = financeService.getTotalAccountNoPage(null);
    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
    for (FinanceAmountDto financeAmountDto : financeAmountDtos) {
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("assetCode", financeAmountDto.getAssetCode());
      resMap.put("amountAvailable", financeAmountDto.getAmountAvailable());
      resMap.put("amountLock", financeAmountDto.getAmountLock());
      resMap.put("amountTotal", financeAmountDto.getAmountTotal());
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName("platAssetStatus.ftl", resultMerStlMap, STATUS_PATH_NAME);

    //将数据存储到数据库中
    try {
      List<BeginingBalance> beginingBalances = financeAmountDtos.stream().map(financeAmountDto -> {
        BeginingBalance balance = new BeginingBalance();
        balance.setAssetCode(financeAmountDto.getAssetCode());
        balance.setAmount_available(financeAmountDto.getAmountAvailable());
        balance.setAmount_lock(financeAmountDto.getAmountLock());
        balance.setAmount_total(financeAmountDto.getAmountTotal());
        balance.setCreateDate(new Date());
        return balance;
      }).collect(Collectors.toList());
      beginingBalanceService.insertBatch(beginingBalances);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
