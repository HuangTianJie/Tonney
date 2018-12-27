package com.gop.job;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.github.pagehelper.PageInfo;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.BeginingBalance;
import com.gop.domain.ConfigAsset;
import com.gop.domain.FinanceTotal;
import com.gop.domain.StatisticeResult;
import com.gop.domain.WalletBalanceTotal;
import com.gop.domain.WalletInOutTotal;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.domain.enums.WithdrawCoinOrderStatus;
import com.gop.finance.dto.PlatAssetProcessDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.mapper.dto.FinanceAmountDto;
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
@Component("financeTotalJob")
@EnableConfigurationProperties(MailConfig.class)
public class FinanceTotalJob implements SimpleJob {
  private final String TOTAL_FTL ="dailyTotal.ftl";
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
  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    log.info("financeTotalJob is starting.{}",fileDate);
    String TOTAL_PATH_NAME = mailConfig.getReportRoot()+ "total" + fileDate + ".xls";
    Date beginDate = DateUtils.add(-1);
    Date endDate = new Date();
    List<WalletBalanceTotal> walletBalanceTotal = financeService.queryWalletTotal("",endDate, 1, -1);
    List<WalletInOutTotal> walletInOutTotal = financeService.queryWalletInOutTotal("",beginDate,endDate,1, -1);
    PageInfo<FinanceAmountDto> assetTotal = financeService.getTotalAccountByAssetCode(null, 1, 999999);
    List<PlatAssetProcessDto> assetFlowTotal = buildAssetsFlowTotal(beginDate, endDate, 1, 999999);
    PageInfo<FinanceTotal> list = countFinanceTotal(walletBalanceTotal, walletInOutTotal, assetTotal.getList(), assetFlowTotal);

    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
    for(FinanceTotal financeTotal : list.getList()){
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("assetCode",financeTotal.getAssetCode());
      resMap.put("inoutBalance",financeTotal.getInoutBalance());
      resMap.put("walletBalance",financeTotal.getWalletBalance());
      resMap.put("walletDiff",financeTotal.getWalletDiff());
      resMap.put("platWalletDiff",financeTotal.getPlatWalletDiff());
      resMap.put("platDiff",financeTotal.getPlatDiff());
      resMap.put("assetBalance",financeTotal.getAssetBalance());
      resMap.put("assetProcess",financeTotal.getDepositWithdrawBalance());
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName(TOTAL_FTL,resultMerStlMap, TOTAL_PATH_NAME);
  }

  private PageInfo<FinanceTotal> countFinanceTotal(List<WalletBalanceTotal> walletBalanceTotals, List<WalletInOutTotal> walletInOutTotals,
                                                   List<FinanceAmountDto> financeAmount, List<PlatAssetProcessDto> platAssetProcess) {
    Map<String,BigDecimal> balanceMap = new HashMap<>();
    Map<String,BigDecimal> inoutMap = new HashMap<>();
    Map<String,BigDecimal> assetMap = new HashMap<>();
    Map<String,BigDecimal> processMap = new HashMap<>();
    walletBalanceTotals.forEach(balance -> balanceMap.put(balance.getAssetCode(),balance.getTotalBalance()));
    walletInOutTotals.forEach(inout -> inoutMap.put(inout.getAssetCode(),inout.getTotalAmount()));
    financeAmount.forEach(asset -> assetMap.put(asset.getAssetCode(),asset.getAmountTotal()));
    platAssetProcess.forEach(process -> processMap.put(process.getSymbol(),process.getCulBalance()));

    List<FinanceTotal> list = new ArrayList<>();
    for (String key: assetMap.keySet()){
      FinanceTotal total = new FinanceTotal();
      total.setAssetCode(key);
      total.setWalletBalance(balanceMap.get(key)==null?BigDecimal.ZERO:balanceMap.get(key));
      total.setInoutBalance(inoutMap.get(key)==null?BigDecimal.ZERO:inoutMap.get(key));
      total.setAssetBalance(assetMap.get(key)==null?BigDecimal.ZERO:assetMap.get(key));
      total.setDepositWithdrawBalance(processMap.get(key)==null?BigDecimal.ZERO:processMap.get(key));
      BigDecimal walletDiff =total.getInoutBalance().subtract(total.getWalletBalance());
      BigDecimal platDiff = total.getAssetBalance().subtract(total.getDepositWithdrawBalance());
      BigDecimal platWalletDiff = walletDiff.subtract(platDiff);
      total.setPlatDiff(platDiff);
      total.setWalletDiff(walletDiff);
      total.setPlatWalletDiff(platWalletDiff);
      list.add(total);
    }
    return new PageInfo<>(list);
  }

  private List<PlatAssetProcessDto> buildAssetsFlowTotal(Date beginDate,Date endDate,Integer pageNo,Integer pageSize) {
    //查询币种
    PageInfo<ConfigAsset> pageInfo = configAssetService.getAvailableAssetCode(null, pageNo, pageSize);
    List<PlatAssetProcessDto> platAssetProcessDtoList = Lists.newArrayList();
    try {
      beginDate = DateUtils.parseDate(
          org.apache.commons.lang3.time.DateFormatUtils.format(beginDate, "yyyy-MM-dd") + " 00:00:00");
    } catch (ParseException e) {
      e.printStackTrace();
    }
    //获取期初金额
    Map<String,BigDecimal> beginBalanceMaps =beginingBalanceService.getBeginingBalance(null, beginDate).stream()
                                                                   .collect(Collectors.toMap(BeginingBalance::getAssetCode, BeginingBalance::getAmount_total));
    //查询转入总额
    Map<String,BigDecimal> depositMaps = depositCoinQueryService.getTotalDeposits(null, DepositCoinAssetStatus.SUCCESS, beginDate, endDate)
                                                                .stream().collect(Collectors.toMap(StatisticeResult::getSymbol, StatisticeResult::getTradeNumber));

    //查询提现相关金额
    Map<String,Map<String,BigDecimal>> withdrawMap = withdrawCoinQueryService.getTotalWithdrawedCoinValue(null,beginDate,endDate);
    Map<String,BigDecimal> numberMaps = withdrawMap.get("number");
    Map<String,BigDecimal> txfeeMaps = withdrawMap.get("txfee");

    //管理员调整资产
    Map<String,BigDecimal> brokerMaps =brokerAssetOperDetailService.getBroketAssetByAsset(null, beginDate,endDate).stream()
                                                                   .collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

    //交易手续费
    Map<String,BigDecimal> buyFees = tradeRecordService.culBuyFeeByAsset(null,beginDate,endDate);
    Map<String,BigDecimal> sellFees = tradeRecordService.culSellFeeByAsset(null,beginDate,endDate);
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

    for(ConfigAsset configAsset : pageInfo.getList()){
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

      PlatAssetProcessDto platAssetProcessDto = new PlatAssetProcessDto();
      platAssetProcessDto.setSymbol(asset);
      platAssetProcessDto.setBeginBalance(beginingBalance);
      platAssetProcessDto.setDepositBalance(depositAmount);
      platAssetProcessDto.setWithdraw(successNumber.add(failureNumber).add(refuseNumber).add(waitNumber).add(unknowNumber).add(processNumber));
      platAssetProcessDto.setWithdrawUnknow(unknowNumber.add(waitNumber).add(processNumber));
      platAssetProcessDto.setWithdrawRefuse(failureNumber.add(refuseNumber));
      platAssetProcessDto.setWithdrawSuccess(successNumber);
      platAssetProcessDto.setWithdrawFee(txfee);
      platAssetProcessDto.setBrokenAsset(brokerAsset);
      platAssetProcessDto.setTradeFee(tradeFee);
      platAssetProcessDto.setOther(otherAmount);
      platAssetProcessDto.setEndBalance(endBalance);
      platAssetProcessDto.setCulBalance(culBalance);
      platAssetProcessDtoList.add(platAssetProcessDto);
    }
    return platAssetProcessDtoList;
  }
}
