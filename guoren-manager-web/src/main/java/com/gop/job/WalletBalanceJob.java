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
import com.gop.config.MailConfig;
import com.gop.domain.BeginingBalance;
import com.gop.domain.ConfigAsset;
import com.gop.domain.StatisticeResult;
import com.gop.domain.WalletBalanceTotal;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.domain.enums.WithdrawCoinOrderStatus;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.match.service.TradeRecordService;
import com.gop.util.DateUtils;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
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
@Component("walletBalanceJob")
@EnableConfigurationProperties(MailConfig.class)
public class WalletBalanceJob implements SimpleJob {
  private final String WALLET_BALANCE ="walletBalance.ftl";

  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private ExportExcelService exportExcelService;
  @Autowired
  private FinanceService financeService;

  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    String WALLET_PATH_NAME = mailConfig.getReportRoot()+ "walletBalance" + fileDate + ".xls";
    List<WalletBalanceTotal> context = financeService.queryWalletTotal("", new Date(), 1, -1);

    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
    for(WalletBalanceTotal walletBalanceTotal : context){
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("assetCode",walletBalanceTotal.getAssetCode());
      resMap.put("hotBalance",walletBalanceTotal.getHotBalance());
      resMap.put("coldBalance",walletBalanceTotal.getColdBalance());
      resMap.put("total",walletBalanceTotal.getHotBalance().add(walletBalanceTotal.getColdBalance()));
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName(WALLET_BALANCE,resultMerStlMap, WALLET_PATH_NAME);
  }
}
