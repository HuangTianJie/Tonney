package com.gop.job;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinAddressService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.BeginingBalance;
import com.gop.domain.ConfigAsset;
import com.gop.domain.PlatAssetProcess;
import com.gop.domain.StatisticeResult;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.domain.enums.WithdrawCoinOrderStatus;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.PlatAssetProessService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.match.service.TradeRecordService;
import com.gop.util.DateUtils;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
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
@Component("platAssetProessJob")
@EnableConfigurationProperties(MailConfig.class)
public class PlatAssetProessJob implements SimpleJob {
  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private BeginingBalanceService beginingBalanceService;
  @Autowired
  private ExportExcelService exportExcelService;
  @Autowired
  private FinanceService financeService;
  @Autowired
  private ConfigAssetService configAssetService;
  @Autowired
  private DepositCoinQueryService depositCoinQueryService;
  @Autowired
  private WithdrawCoinQueryService withdrawCoinQueryService;
  @Autowired
  private BrokerAssetOperDetailService brokerAssetOperDetailService;
  @Autowired
  private TradeRecordService tradeRecordService;
  @Autowired
  private PlatAssetProessService platAssetProessService;

  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    String PROCESS_PATH_NAME = mailConfig.getReportRoot()+ "platAssetProess" + fileDate + ".xls";
    //查询币种
    List<ConfigAsset> configAssets = configAssetService.getAvailableAssetCode();
    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
    Date endDate = new Date();
    Date startDate = DateUtils.add(-1);
    //获取期初金额
    Map<String,BigDecimal> beginBalanceMaps =beginingBalanceService.getBeginingBalance(null, startDate).stream()
                                                                   .collect(Collectors.toMap(BeginingBalance::getAssetCode,BeginingBalance::getAmount_total));
    //查询转入总额
    Map<String,BigDecimal> depositMaps = depositCoinQueryService.getTotalDeposits(null, DepositCoinAssetStatus.SUCCESS, startDate, endDate)
                                                                .stream().collect(Collectors.toMap(StatisticeResult::getSymbol, StatisticeResult::getTradeNumber));

    //查询提现相关金额
    Map<String,Map<String,BigDecimal>> withdrawMap = withdrawCoinQueryService.getTotalWithdrawedCoinValue(null,startDate,endDate);
    Map<String,BigDecimal> numberMaps = withdrawMap.get("number");
    Map<String,BigDecimal> txfeeMaps = withdrawMap.get("txfee");

    //管理员调整资产
    Map<String,BigDecimal> brokerMaps =brokerAssetOperDetailService.getBroketAssetByAsset(null, startDate,endDate).stream()
                                                                   .collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

    //交易手续费
    Map<String,BigDecimal> buyFees = tradeRecordService.culBuyFeeByAsset(null,startDate,endDate);
    Map<String,BigDecimal> sellFees = tradeRecordService.culSellFeeByAsset(null,startDate,endDate);
    //期末余额
    Map<String,BigDecimal> financeMap = Maps.newHashMap();
    Date now = null;
    try {
      now = DateUtils.parseDate(DateUtils.formatDate(new Date(), "yyyy-MM-dd")+" 00:00:00");
    } catch (ParseException e) {
      e.printStackTrace();
    }
    if(endDate == null|| endDate.after(now) ) {
      financeMap = financeService.queryTotalCountByAssetCode(null)
                                 .stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));
    }else{
      financeMap =beginingBalanceService.getBeginingBalance(null, endDate).stream()
                                        .collect(Collectors.toMap(BeginingBalance::getAssetCode,BeginingBalance::getAmount_total));
    }
    List<PlatAssetProcess> platAssetProcessList = Lists.newArrayList();
    for(ConfigAsset configAsset : configAssets){
      String asset = configAsset.getAssetCode();
      //获取期初金额
      BigDecimal beginingBalance = beginBalanceMaps.get(asset)==null ? BigDecimal.ZERO:beginBalanceMaps.get(asset);

      //查询转入总额
      BigDecimal depositAmount = depositMaps.get(asset) ==null ? BigDecimal.ZERO:depositMaps.get(asset);

      //管理员调整资产
      BigDecimal brokerAsset = brokerMaps.get(asset)==null ? BigDecimal.ZERO:brokerMaps.get(asset);

      //交易手续费
      BigDecimal buyFee = buyFees.get(asset)==null?BigDecimal.ZERO:buyFees.get(asset);
      BigDecimal sellFee = sellFees.get(asset)==null?BigDecimal.ZERO:sellFees.get(asset);
      BigDecimal tradeFee = buyFee.add(sellFee);
      //其他
      BigDecimal otherAmount = BigDecimal.ZERO;
      //期末余额
      BigDecimal endBalance =financeMap.get(asset)==null ? BigDecimal.ZERO:financeMap.get(asset);
      BigDecimal successNumber = numberMaps.get(asset + "_" + WithdrawCoinOrderStatus.SUCCESS) == null ? BigDecimal.ZERO : numberMaps.get(asset + "_" + WithdrawCoinOrderStatus.SUCCESS);
      BigDecimal refuseNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.REFUSE)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.REFUSE);
      BigDecimal failureNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.FAILURE)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.FAILURE);
      BigDecimal waitNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.WAIT)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.WAIT);
      BigDecimal unknowNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.UNKNOWN)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.UNKNOWN);
      BigDecimal processNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.PROCESSING)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.PROCESSING);
      BigDecimal txfee = txfeeMaps.get(asset)==null?BigDecimal.ZERO:txfeeMaps.get(asset);
      //计算余额
      BigDecimal culBalance = beginingBalance.add(depositAmount).subtract(successNumber).subtract(txfee)
                                             .add(brokerAsset).subtract(tradeFee).add(otherAmount);

      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("symbol",asset);
      resMap.put("beginBalance",beginingBalance);
      resMap.put("depositBalance",depositAmount);
      resMap.put("withdraw",successNumber.add(failureNumber).add(refuseNumber).add(waitNumber).add(unknowNumber).add(processNumber));
      resMap.put("withdrawUnknow",unknowNumber.add(waitNumber).add(processNumber));
      resMap.put("withdrawSuccess",successNumber);
      resMap.put("withdrawRefuse",failureNumber.add(refuseNumber));
      resMap.put("withdrawFee",txfee);
      resMap.put("brokenAsset",brokerAsset);
      resMap.put("tradeFee",tradeFee);
      resMap.put("other",otherAmount);
      resMap.put("endBalance",endBalance);
      resMap.put("culBalance",culBalance);
      lst.add(resMap);

      PlatAssetProcess platAssetProcess = new PlatAssetProcess();
      platAssetProcess.setAssetCode(asset);
      platAssetProcess.setBeginBalance(beginingBalance);
      platAssetProcess.setDepositBalance(depositAmount);
      platAssetProcess.setWithdrawTotal(successNumber.add(failureNumber).add(refuseNumber).add(waitNumber).add(unknowNumber).add(processNumber));
      platAssetProcess.setWithdrawUnknow(unknowNumber.add(waitNumber).add(processNumber));
      platAssetProcess.setWithdrawSuccess(successNumber);
      platAssetProcess.setWithdrawRefuse(failureNumber.add(refuseNumber));
      platAssetProcess.setWithdrawFee(txfee);
      platAssetProcess.setBrokenAssetBalance(brokerAsset);
      platAssetProcess.setTradeFee(tradeFee);
      platAssetProcess.setOther(otherAmount);
      platAssetProcess.setEndBalance(endBalance);
      platAssetProcess.setCulBalance(culBalance);
      platAssetProcess.setCreateDate(new Date());
      platAssetProcess.setStatus(1);
      platAssetProcessList.add(platAssetProcess);
    }


    resultMerStlMap.put("resultList", lst);
    //生成excel文件
    exportExcelService.createTemplateXlsByFileName("platAssetProess.ftl",resultMerStlMap, PROCESS_PATH_NAME );
    if(platAssetProessService.selectCount(new Date())> 0){
      platAssetProessService.updateStatus(new Date());
    }
    platAssetProessService.insertBatch(platAssetProcessList);
  }
}
