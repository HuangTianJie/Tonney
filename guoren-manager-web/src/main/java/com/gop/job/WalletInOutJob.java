package com.gop.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.service.FinanceService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.WalletInOutTotal;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Created by YAO on 2018/5/29.
 */

@Slf4j
@Component("walletInOutJob")
@EnableConfigurationProperties(MailConfig.class)
public class WalletInOutJob implements SimpleJob {
  private final String WALLET_INOUT_FTL ="walletInOut.ftl";
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
    String INOUT_PATH_NAME = mailConfig.getReportRoot()+ "walletInOut" + fileDate + ".xls";
    Date beginDate = DateUtils.add(new Date(), 5, -1);
    Date endDate = new Date();
    List<WalletInOutTotal> context = financeService.queryWalletInOutTotal("", beginDate, endDate, 1, -1);

    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
    for(WalletInOutTotal walletInOutTotal : context){
      BigDecimal depositAmount =walletInOutTotal.getDepositAmount();
      BigDecimal withDrawAmount =walletInOutTotal.getWithDrawAmount();
      BigDecimal preAmount =walletInOutTotal.getPreAmount();
      BigDecimal total = depositAmount.add(withDrawAmount).add(preAmount);
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("assetCode",walletInOutTotal.getAssetCode());
      resMap.put("depositAmount",depositAmount);
      resMap.put("withDrawAmount",withDrawAmount);
      resMap.put("preAmount",preAmount);
      resMap.put("total",total);
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName(WALLET_INOUT_FTL,resultMerStlMap, INOUT_PATH_NAME);
  }
}
