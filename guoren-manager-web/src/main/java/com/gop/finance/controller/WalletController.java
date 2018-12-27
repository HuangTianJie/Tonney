package com.gop.finance.controller;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import com.alibaba.fastjson.serializer.ListSerializer;
import com.github.pagehelper.PageInfo;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.BeginingBalance;
import com.gop.domain.ConfigAsset;
import com.gop.domain.EmailLog;
import com.gop.domain.FinanceTotal;
import com.gop.domain.WalletBalance;
import com.gop.domain.WalletBalanceTotal;
import com.gop.domain.WalletInOut;
import com.gop.domain.WalletInOutTotal;
import com.gop.domain.enums.AssetStatus;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.domain.enums.ServiceCode;
import com.gop.domain.enums.ServiceProvider;
import com.gop.domain.enums.SysCode;
import com.gop.exception.AppException;
import com.gop.finance.dto.FinanceTotalDto;
import com.gop.finance.dto.PlatAssetProcessDto;
import com.gop.finance.dto.WalletBalacneTotalDto;
import com.gop.finance.dto.WalletBalanceDto;
import com.gop.finance.dto.WalletInOutDto;
import com.gop.finance.dto.WalletInOutTotalDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.match.service.TradeRecordService;
import com.gop.mode.vo.PageModel;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.EmailLogService;
import com.gop.sms.service.IEmailService;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;
import com.gop.util.PageUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by YAO on 2018/4/24.
 */
@RestController("WalletController")
@RequestMapping("/wallet")
@Slf4j
@EnableConfigurationProperties(MailConfig.class)
public class WalletController {
  private static final String WALLET_BALANCE_FTL ="walletBalance.ftl";
  private static final String WALLET_INOUT_FTL ="walletInOut.ftl";
  private static final String WALLET_BALANCE_QUERY_FTL ="walletBalanceQuery.ftl";
  private static final String WALLET_INOUT_QUERY_FTL ="walletInOutQuery.ftl";

  private static final String BALANCE_TOTAL_NAME = "walletBalanceTotal.xls";
  private static final String INOUT_TOTAL_NAME = "walletInOutTotal.xls";
  private static final String WALLET_BALANCE_NAME = "walletBalance.xls";
  private static final String WALLET_INOUT_NAME = "walletInOut.xls";

  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private FinanceService financeService;
  @Autowired
  private ConfigAssetService configAssetService;
  @Autowired
  private ExportExcelService exportExcelService;

  private static final ListeningExecutorService guavaExecutor = MoreExecutors
      .listeningDecorator(Executors.newFixedThreadPool(10));
  /**
   * 区块链钱包余额
   * @param assetCode
   * @param date
   * @param account
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/wallet-list", method = RequestMethod.GET)
  // @ApiOperation("钱包资产列表")
  public PageModel<WalletBalanceDto> walletBalacneList(@AuthForHeader AuthContext context,
                                                       @RequestParam(value = "type", required = false,defaultValue = "") String type,
                                                       @RequestParam(value = "assetCode", required = false,defaultValue = "") String assetCode,
                                                       @RequestParam(value = "date")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date,
                                                       @RequestParam(value = "account", required = false,defaultValue = "") String account,
                                                       @RequestParam("pageNo") Integer pageNo,
                                                       @RequestParam("pageSize") Integer pageSize) {
    if (!assetCode.equalsIgnoreCase("") && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)){
      throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
    }
    List<WalletBalance> info = financeService.queryWalletBalance(type,assetCode, date, account, pageNo, pageSize);
    PageUtil<WalletBalance> pageMaker = new PageUtil<>();
    List<WalletBalance> list = pageMaker.ListPage(info,pageNo,pageSize);

    PageModel<WalletBalanceDto> pageModel = new PageModel<>();
    pageModel.setPageSize(pageSize);
    pageModel.setPageNo(pageNo);
    pageModel.setPageNum(pageMaker.pages);
    pageModel.setTotal(pageMaker.total);
    pageModel.setList(list.stream().map(wbt -> new WalletBalanceDto(wbt)).collect(Collectors.toList()));
    return pageModel;
  }

  /**
   * 区块链钱包余额汇总
   * @param date
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/wallet-total", method = RequestMethod.GET)
  // @ApiOperation("钱包资产汇总")
  public PageModel<WalletBalacneTotalDto> walletBalanceTotal(@AuthForHeader AuthContext context,
                                                             @RequestParam(value = "date")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date,
                                                             @RequestParam("pageNo") Integer pageNo,
                                                             @RequestParam("pageSize") Integer pageSize) {
    List<WalletBalanceTotal> info = financeService.queryWalletTotal("",date, pageNo, pageSize);
    PageUtil<WalletBalanceTotal> pageMaker = new PageUtil<>();
    List<WalletBalanceTotal> list = pageMaker.ListPage(info,pageNo,pageSize);
    PageModel<WalletBalacneTotalDto> pageModel = new PageModel<>();
    pageModel.setPageSize(pageSize);
    pageModel.setPageNo(pageNo);
    pageModel.setPageNum(pageMaker.pages);
    pageModel.setTotal(pageMaker.total);
    pageModel.setList(list.stream().map(wbt -> new WalletBalacneTotalDto(wbt)).collect(Collectors.toList()));
    return pageModel;
  }

  /**
   * 区块链充值提现记录
   * @param account
   * @param opt
   * @param assetCode
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                              @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/inout-list", method = RequestMethod.GET)
  // @ApiOperation("钱包充值提现记录")
  public PageModel<WalletInOutDto> inoutList(@AuthForHeader AuthContext context,
                                             @RequestParam(value = "beginDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
                                             @RequestParam(value = "endDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                             @RequestParam(value = "opt", required = false,defaultValue = "") String opt,
                                             @RequestParam(value = "assetCode", required = false,defaultValue = "") String assetCode,
                                             @RequestParam(value = "account", required = false,defaultValue = "") String account,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize) {
    if (!assetCode.equalsIgnoreCase("") && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)){
      throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
    }
//    Long period = Long.valueOf("3"); // 限制3天
//    if (DateUtils.subtract(beginDate,endDate) > period){
//      endDate = DateUtils.add(beginDate, Calendar.DATE,3);
//    }
    List<WalletInOut>info = financeService.queryWalletInOut(beginDate,endDate,opt, assetCode, account, pageNo, pageSize);
    PageUtil<WalletInOut> pageMaker = new PageUtil<>();
    List<WalletInOut> list = pageMaker.ListPage(info,pageNo,pageSize);
    PageModel<WalletInOutDto> pageModel = new PageModel<>();
    pageModel.setPageSize(pageSize);
    pageModel.setPageNo(pageNo);
    pageModel.setPageNum(pageMaker.pages);
    pageModel.setTotal(pageMaker.total);
    pageModel.setList(list.stream().map(wbt -> new WalletInOutDto(wbt)).collect(Collectors.toList()));
    return pageModel;
  }

  /**
   * 区块链钱包余额汇总
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/inout-total", method = RequestMethod.GET)
  // @ApiOperation("钱包充值提现汇总")
  public PageModel<WalletInOutTotalDto> inoutTotal(@AuthForHeader AuthContext context,
                                                   @RequestParam(value = "beginDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
                                                   @RequestParam(value = "endDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam("pageSize") Integer pageSize) {
//    Long period = Long.valueOf("3"); // 限制3天
//    if (DateUtils.subtract(beginDate,endDate) > period){
//      endDate = DateUtils.add(beginDate, Calendar.DATE,3);
//    }
    List<WalletInOutTotal> info = financeService.queryWalletInOutTotal("",beginDate,endDate,pageNo, pageSize);
    PageUtil<WalletInOutTotal> pageMaker = new PageUtil<>();
    List<WalletInOutTotal> list = pageMaker.ListPage(info,pageNo,pageSize);
    PageModel<WalletInOutTotalDto> pageModel = new PageModel<>();
    pageModel.setPageSize(pageSize);
    pageModel.setPageNo(pageNo);
    pageModel.setPageNum(pageMaker.pages);
    pageModel.setTotal(pageMaker.total);
    pageModel.setList(list.stream().map(wbt -> new WalletInOutTotalDto(wbt)).collect(Collectors.toList()));
    return pageModel;
  }



  /**
   * 区块链钱包余额导出
   * @param date
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/wallet-list/export", method = RequestMethod.GET)
  // @ApiOperation("钱包资产列表")
  public String exportWalletBalacneList(@AuthForHeader AuthContext context,
                                        @RequestParam(value = "date")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date){
    List<WalletBalance> info = financeService.queryWalletBalance("", "", date, "", 1, -1);
    Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
    List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
    for (WalletBalance walletBalance : info) {
      Map<String, Object> resMap = new HashMap<String, Object>();
      resMap.put("type", walletBalance.getWalletType());
      resMap.put("assetCode", walletBalance.getAssetCode());
      resMap.put("account", walletBalance.getAccount());
      resMap.put("balance", walletBalance.getBalance());
      lst.add(resMap);
    }
    resultMerStlMap.put("resultList", lst);
    exportExcelService.createTemplateXlsByFileName(WALLET_BALANCE_QUERY_FTL, resultMerStlMap, mailConfig.getReportRoot()+WALLET_BALANCE_NAME);
    return mailConfig.getReportDownload()+WALLET_BALANCE_NAME;
  }

  /**
   * 区块链充值提现记录导出
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/inout-list/export", method = RequestMethod.GET)
  // @ApiOperation("钱包充值提现记录")
  public String exportInoutList(@AuthForHeader AuthContext context,
                          @RequestParam(value = "beginDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
                          @RequestParam(value = "endDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
//    Long period = Long.valueOf("3"); // 限制3天
//    if (DateUtils.subtract(beginDate,endDate) > period){
//      endDate = DateUtils.add(beginDate, Calendar.DATE,3);
//    }
      List<WalletInOut>info = financeService.queryWalletInOut(beginDate,endDate,"", "", "", 1, -1);
      Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
      List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
      for(WalletInOut walletInOut : info){
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("time",walletInOut.getUpdateTime());
        resMap.put("opt",walletInOut.getOpt());
        resMap.put("assetCode",walletInOut.getAssetCode());
        String address = walletInOut.getOpt().equalsIgnoreCase("IN")?walletInOut.getToAccount():walletInOut.getFromAccount();
        resMap.put("account",address);
        resMap.put("amount",walletInOut.getAmount());
        lst.add(resMap);
      }
      resultMerStlMap.put("resultList", lst);
      exportExcelService.createTemplateXlsByFileName(WALLET_INOUT_QUERY_FTL,resultMerStlMap, mailConfig.getReportRoot()+WALLET_INOUT_NAME);
    return mailConfig.getReportDownload()+WALLET_INOUT_NAME;
  }

  /**
   * 区块链钱包余额汇总导出(发邮件)
   * @param date
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/wallet-total/export", method = RequestMethod.GET)
  // @ApiOperation("钱包资产汇总")
  public String exportWalletBalanceTotal(@AuthForHeader AuthContext context,
                                   @RequestParam(value = "date")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) {
      List<WalletBalanceTotal> walletTotal = financeService.queryWalletTotal("",date, 1, -1);
      Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
      List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
      for(WalletBalanceTotal walletBalanceTotal : walletTotal){
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("assetCode",walletBalanceTotal.getAssetCode());
        resMap.put("hotBalance",walletBalanceTotal.getHotBalance());
        resMap.put("coldBalance",walletBalanceTotal.getColdBalance());
        resMap.put("total",walletBalanceTotal.getHotBalance().add(walletBalanceTotal.getColdBalance()));
        lst.add(resMap);
      }
      resultMerStlMap.put("resultList", lst);
      exportExcelService.createTemplateXlsByFileName(WALLET_BALANCE_FTL,resultMerStlMap, mailConfig.getReportRoot()+BALANCE_TOTAL_NAME);
    return mailConfig.getReportDownload()+BALANCE_TOTAL_NAME;
    }
  /**
   * 区块链钱包充值提现汇总导出
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/inout-total/export", method = RequestMethod.GET)
  // @ApiOperation("钱包充值提现汇总")
  public String exportInoutTotal(@AuthForHeader AuthContext context,
                           @RequestParam(value = "beginDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
                           @RequestParam(value = "endDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
//    Long period = Long.valueOf("3"); // 限制3天
//    if (DateUtils.subtract(beginDate,endDate) > period){
//      endDate = DateUtils.add(beginDate, Calendar.DATE,3);
//    }
      List<WalletInOutTotal> w = financeService.queryWalletInOutTotal("",beginDate, endDate, 1, -1);
      Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
      List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
      for(WalletInOutTotal walletInOutTotal : w){
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
      exportExcelService.createTemplateXlsByFileName(WALLET_INOUT_FTL,resultMerStlMap, mailConfig.getReportRoot()+INOUT_TOTAL_NAME);
      return mailConfig.getReportDownload()+INOUT_TOTAL_NAME;
    }

  /**
   * 总报表
   */
  @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
                          @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
  @RequestMapping(value = "/total/export", method = RequestMethod.GET)
  // @ApiOperation("汇总")
  public void emailFinanceTotal(@AuthForHeader AuthContext context,
                                @RequestParam(value = "assetCode", required = false,defaultValue = "") String assetCode,
                                @RequestParam(value = "beginDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginDate,
                                @RequestParam(value = "endDate")  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
      if (!assetCode.equalsIgnoreCase("") && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)){
        throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
      }

    guavaExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          financeService.sendEmailReport(assetCode,beginDate,endDate);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          log.error("发送错误:message={}", e.getMessage());
        }
      }
    });
  }
}
