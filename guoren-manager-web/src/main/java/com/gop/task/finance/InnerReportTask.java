package com.gop.task.finance;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.domain.*;
import com.gop.domain.enums.*;
import com.gop.finance.dto.AssetDto;
import com.gop.finance.dto.PlatAssetProcessDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.PlatAssetProessService;
import com.gop.finance.service.UserStatisticsService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.match.service.TradeRecordService;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.EmailLogService;
import com.gop.sms.service.IEmailService;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wuyanjie on 2018/5/7.
 */
@Component
@Slf4j
public class InnerReportTask {
    @Value("${mail.to-user}")
    private String toUser;
    @Value("${mail.report-subject}")
    private String subject;
    @Value("${mail.report-text}")
    private String text;

    private final String WALLET_BALANCE_FTL ="walletBalance.ftl";
    private final String WALLET_INOUT_FTL ="walletInOut.ftl";
    private final String TOTAL_FTL ="dailyTotal.ftl";

    private static final String rootPath = "/data/app/static/exchange_manager/data/";

    private static final String STATUS_USRE_PATH_NAME = rootPath + "userAsset"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String STATUS_PATH_NAME = rootPath + "platAssetStatus"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String PROESS_PATH_NAME = rootPath + "platAssetProess"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String WALLET_PATH_NAME = rootPath + "walletBalance"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String INOUT_PATH_NAME = rootPath + "walletInOut"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String TOTAL_PATH_NAME = rootPath + "total"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";

    @Autowired
    private ConfigAssetService configAssetService;
    @Autowired
    private BeginingBalanceService beginingBalanceService;
    @Autowired
    private DepositCoinQueryService depositCoinQueryService;
    @Autowired
    private WithdrawCoinQueryService withdrawCoinQueryService;
    @Autowired
    private BrokerAssetOperDetailService brokerAssetOperDetailService;
    @Autowired
    private TradeRecordService tradeRecordService;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private ExportExcelService exportExcelService;
    @Autowired
    private IEmailService iEmailService;
    @Autowired
    private EmailLogService emailLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserStatisticsService userStatisticsService;
  @Autowired
  private PlatAssetProessService platAssetProessService;

//    @Scheduled(cron = "0 0 0 * * ? ")
    @Transactional
    public void userAssetStatus() {
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
//  @Scheduled(cron = "0 0 0 * * ? ")
  @Transactional
  public void walletBalanceDailyReport() {
    List<WalletBalanceTotal> context = financeService.queryWalletTotal("",new Date(), 1, -1);

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
    exportExcelService.createTemplateXlsByFileName(WALLET_BALANCE_FTL,resultMerStlMap, WALLET_PATH_NAME);
  }

//  @Scheduled(cron = "0 0 0 * * ? ")
  @Transactional
  public void walletInOutDailyReport() {
    Date beginDate = DateUtils.add(new Date(),5,-1);
    Date endDate = new Date();
    List<WalletInOutTotal> context = financeService.queryWalletInOutTotal("",beginDate, endDate, 1, -1);

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
//  @Scheduled(cron = "0 0 0 * * ? ")
  @Transactional
  public void totalDailyReport() {
    Date beginDate = DateUtils.add(-1);
    Date endDate = new Date();
    List<WalletBalanceTotal> walletBalanceTotal = financeService.queryWalletTotal("",endDate, 1, -1);
    List<WalletInOutTotal> walletInOutTotal = financeService.queryWalletInOutTotal("",beginDate,endDate,1, -1);
    PageInfo<FinanceAmountDto> assetTotal = financeService.getTotalAccountByAssetCode(null, 1, 999999);
    List<PlatAssetProcessDto> assetFlowTotal = buildAssetsFlowTotal(beginDate, endDate, 1, 999999);
    PageInfo<FinanceTotal> list = countFinanceTotal(walletBalanceTotal, walletInOutTotal, assetTotal.getList(),assetFlowTotal);

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
              .collect(Collectors.toMap(BeginingBalance::getAssetCode,BeginingBalance::getAmount_total));
      //查询转入总额
      Map<String,BigDecimal> depositMaps = depositCoinQueryService.getTotalDeposits(null,DepositCoinAssetStatus.SUCCESS,beginDate,endDate)
              .stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

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
          BigDecimal successNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.SUCCESS)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.SUCCESS);
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
//    @Scheduled(cron = "0 0 0 * * ? ")
    @Transactional
    public void platAssetStatus() {
        List<FinanceAmountDto> financeAmountDtos = financeService.getTotalAccountNoPage(null);
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(FinanceAmountDto financeAmountDto : financeAmountDtos){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("assetCode",financeAmountDto.getAssetCode());
            resMap.put("amountAvailable",financeAmountDto.getAmountAvailable());
            resMap.put("amountLock",financeAmountDto.getAmountLock());
            resMap.put("amountTotal",financeAmountDto.getAmountTotal());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platAssetStatus.ftl",resultMerStlMap, STATUS_PATH_NAME);

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
//    @Scheduled(cron = "0 0 0 * * ? ")
    @Transactional
    public void platAssetProess() {
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
        Map<String,BigDecimal> depositMaps = depositCoinQueryService.getTotalDeposits(null,DepositCoinAssetStatus.SUCCESS,startDate,endDate)
                .stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

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
            BigDecimal successNumber = numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.SUCCESS)==null?BigDecimal.ZERO:numberMaps.get(asset+"_"+WithdrawCoinOrderStatus.SUCCESS);
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
        exportExcelService.createTemplateXlsByFileName("platAssetProess.ftl",resultMerStlMap, PROESS_PATH_NAME );
        if(platAssetProessService.selectCount(new Date())> 0){
            platAssetProessService.updateStatus(new Date());
        }
        platAssetProessService.insertBatch(platAssetProcessList);
    }
//  @Scheduled(cron = "0 30 0 * * ? ")
    @Transactional
    public void sendEmail(){
        List<File> files = Lists.newArrayList( new File(PROESS_PATH_NAME),new File(STATUS_PATH_NAME),new File(STATUS_USRE_PATH_NAME),new File(WALLET_PATH_NAME),new File(INOUT_PATH_NAME),new File(TOTAL_PATH_NAME));  //增加附加列表
        //发送邮件
        EmailDto email = EmailDto.builder().toUser(Arrays.asList(toUser.split(","))).subject(subject).text(text).fileList(files).build();
        Boolean isSent = iEmailService.sendAttachmentsMail(email);
        buildEmailLog(new Date(),"财务日报表",isSent);
    }

    private void buildEmailLog(Date reportDate,String content, Boolean isSent) {
        EmailLog emailLog = new EmailLog();
        emailLog.setMsgId("");// msgId 短信服务商返回的信息；
        emailLog.setSysCode(SysCode.GTE_MANAGER);
        emailLog.setServiceCode(ServiceCode.REPORT_DAILY);
        emailLog.setServiceProvider(ServiceProvider.TENCENT);
        emailLog.setMsgContent(reportDate +" "+content +" "+ (isSent?"发送成功":"发送失败"));
        emailLog.setEmail(toUser); // 需要传送 邮箱发送地址
        emailLog.setCreateDate(new Date());
        emailLogService.addEmailLog(emailLog);
    }

    /**
     * 用户量每日统计
     */
//    @Scheduled(cron = "0 0 0 * * ? ")
    @Transactional
    public void userDailyStatistic() {
        //每天凌晨跑的是前一天的数据，记录日期为前一天
        Date date = DateUtils.add(-1);
        UserStatistics userStatistics = userService.getDailyStatistic(date);
        if(userStatistics.getTotalUser().compareTo(BigDecimal.ZERO)==0){
            userStatistics.setNativePresent(BigDecimal.ZERO);
            userStatistics.setAdvancedPresent(BigDecimal.ZERO);
        }else {
            userStatistics.setNativePresent(userStatistics.getNativeUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
            userStatistics.setAdvancedPresent(userStatistics.getAdvancedUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
        }
        userStatistics.setStatus(1);
        userStatistics.setCreateDate(date);
        if( userStatisticsService.countUserStatistics(date)> 0){
            userStatisticsService.updateStatus(date);
        }
        userStatisticsService.insertStatistics(userStatistics);
    }
}