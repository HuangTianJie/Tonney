package com.gop.finance.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.ActivityCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.dto.DepositDetailDto;
import com.gop.coin.transfer.dto.WithdrawDetailDto;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.*;
import com.gop.domain.enums.*;
import com.gop.exception.AppException;
import com.gop.finance.dto.BrokerAssetDto;
import com.gop.finance.dto.PlatAssetProcessDto;
import com.gop.finance.dto.TradeOrderDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.PlatAssetProessService;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.TradeRecordService;
import com.gop.mode.vo.PageModel;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wuyanjie on 2018/4/25.
 */
@RestController("InternalAssetFlowController")
@RequestMapping("/internalAssetFlow")
@Slf4j
@EnableConfigurationProperties(MailConfig.class)
public class InternalAssetFlowController {
    private static final String PLAT_ASSET_PROCESS="platAssetProess.xls";
    private static final String USER_TRADE_RECORD="userTradeRecord.xls";
    private static final String BROKER_ASSET="brokerAsset.xls";
    private static final String PLAT_WITH_DRAW ="platWithdraw.xls";
    private static final String PLAT_DEPOSIT ="platDeposit.xls";
    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private BrokerAssetOperDetailService brokerAssetOperDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private MatchOrderService matchOrderService;
    @Autowired
    private ConfigAssetService configAssetService;
    @Autowired
    private DepositCoinQueryService depositCoinQueryService;
    @Autowired
    private WithdrawCoinQueryService withdrawCoinQueryService;
    @Autowired
    private TradeRecordService tradeRecordService;
    @Autowired
    private BeginingBalanceService beginingBalanceService;
    @Autowired
    private ExportExcelService exportExcelService;
    @Autowired
    private PlatAssetProessService platAssetProessService;
    //管理员调整记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/broker/asset", method = RequestMethod.GET)
    public PageModel<BrokerAssetDto> getBrokerAsset(@AuthForHeader AuthContext context,
                                                    @RequestParam(value = "startDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                    @RequestParam(value = "assetCode", required = false) String assetCode,
                                                    @RequestParam(value = "uid", required = false) Integer uid,
                                                    @RequestParam(value = "email", required = false) String email,
                                                    @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

        List<ConfigAsset> availableAssetList = configAssetService.getAvailableAssetCode();
        Set<String> availableAssetSet = availableAssetList.stream().map(c -> c.getAssetCode())
                .collect(Collectors.toSet());
        if (assetCode != null && !availableAssetSet.contains(assetCode)) {
            throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
        }
        if(email != null){
            User user = userService.getUserByEmail(email);
            uid = user.getUid();
        }
        PageInfo<BrokerAssetOperDetail> pageInfo = brokerAssetOperDetailService.getBrokerAssetDetail(uid,assetCode,startDate,endDate,pageNo,pageSize);
        PageModel<BrokerAssetDto> pageModel = new PageModel<>();

        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setList(
                pageInfo.getList().stream().map(brokerAssetOperDetail -> {
                    User user = userService.getUserByUid(brokerAssetOperDetail.getUid());
                    return new BrokerAssetDto(brokerAssetOperDetail,user == null ? null : user.getEmail());
                }).collect(Collectors.toList()));

        return pageModel;
    }

    //用户交易记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/userTradeRecord", method = RequestMethod.GET)
    public PageModel<TradeOrderDto> getUserTradeRecord(@AuthForHeader AuthContext context,
                                                       @RequestParam(value = "brokerId", required = false) Integer brokerId,
                                                       @RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                                       @RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                                       @RequestParam(value = "uid", required = false) Integer uid,
                                                       @RequestParam(value = "tradeFlag", required = false) TradeCoinFlag tradeFlag,
                                                       @RequestParam(value = "symbol", required = false) String symbol,
                                                       @RequestParam(value = "orderType", required = false) TradeCoinType orderType,
                                                       @RequestParam(value = "status", required = false) TradeCoinStatus status,
                                                       @RequestParam("pageNo") Integer pageNo,
                                                       @RequestParam("pageSize") Integer pageSize) {


        PageInfo<TradeOrder> pageInfo = matchOrderService.getTradeOrderRecord(brokerId,tradeFlag,uid,symbol,orderType,status,beginTime,endTime,pageNo,pageSize);
        PageModel<TradeOrderDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setList(
                pageInfo.getList().stream().map(tradeOrder -> new TradeOrderDto(tradeOrder)).collect(Collectors.toList()));

        return pageModel;
    }

    //平台资产过程总表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getAllPlatAsset", method = RequestMethod.GET)
    public PageModel<PlatAssetProcessDto> getAllPlatAsset(@AuthForHeader AuthContext context,
                                                       @RequestParam(value = "startDate", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                       @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                       @RequestParam(value = "symbol", required = false) String symbol,
                                                       @RequestParam("pageNo") Integer pageNo,
                                                       @RequestParam("pageSize") Integer pageSize) {

        PageInfo<PlatAssetProcess> pageInfo = platAssetProessService.selectPlatAssetProcess(endDate,symbol,pageNo,pageSize);
        PageModel<PlatAssetProcessDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setList(pageInfo.getList().stream().map(platAssetProcess -> new PlatAssetProcessDto(platAssetProcess)).collect(Collectors.toList()));

        return pageModel;
    }

    //导出充值记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportDeposit", method = RequestMethod.GET)
    public String exportDeposit(@AuthForHeader AuthContext context,@RequestParam(value = "brokerId", required = false) Integer brokerId,
                              @RequestParam(value = "startDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                              @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                              @RequestParam(value = "assetCode", required = false) String assetCode,
                              @RequestParam(value = "uid", required = false) Integer uid,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "address", required = false) String address) {
        PageInfo<DepositCoinOrderUser> pageInfo = depositCoinQueryService.queryOrder(brokerId, null, email, uid,
                address, null, assetCode,startDate,endDate,email, null,1,10000);
        List<DepositDetailDto> depositDetailDtos = pageInfo.getList().stream().map(order -> new DepositDetailDto(order)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(DepositDetailDto depositDetailDto : depositDetailDtos){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate",depositDetailDto.getCreateDate());
            resMap.put("uid",depositDetailDto.getUid());
            resMap.put("account",depositDetailDto.getAccount());
            resMap.put("assetCode",depositDetailDto.getAssetCode());
            resMap.put("address",depositDetailDto.getAddress());
            resMap.put("amount",depositDetailDto.getAmount());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platDeposit.ftl",resultMerStlMap,  mailConfig.getReportRoot() + "platDeposit.xls");
        return mailConfig.getReportDownload() + PLAT_DEPOSIT;

    }

    //导出提现记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportWithdraw", method = RequestMethod.GET)
    public String exportWithdraw(@AuthForHeader AuthContext context,
                                 @RequestParam(value = "brokerId", required = false) Integer brokerId,
                                 @RequestParam(value = "email", required = false) String email,
                                 @RequestParam(value = "uId", required = false) Integer uId,
                                 @RequestParam(value = "address", required = false) String address,
                                 @RequestParam(value = "assetCode", required = false) String assetCode,
                                 @RequestParam(value = "startDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                 @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                 @RequestParam(value = "status", required = false) WithdrawCoinOrderStatus status) {
        PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryOrder(brokerId, null, email, uId,
                address, null, assetCode,beginTime,endTime,email, status, 1, 10000);
        List<WithdrawDetailDto> withdrawCoinOrderUsers = pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(WithdrawDetailDto withdrawDetailDto : withdrawCoinOrderUsers){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate",withdrawDetailDto.getCreateDate());
            resMap.put("updateDate",withdrawDetailDto.getUpdateDate());
            resMap.put("status",withdrawDetailDto.getStatus());
            resMap.put("uid",withdrawDetailDto.getUid());
            resMap.put("account",withdrawDetailDto.getAccount());
            resMap.put("assetCode",withdrawDetailDto.getAssetCode());
            resMap.put("address",withdrawDetailDto.getAddress());
            resMap.put("amount",withdrawDetailDto.getAmount());
            resMap.put("txFee",withdrawDetailDto.getTxFee());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platWithdraw.ftl",resultMerStlMap, mailConfig.getReportRoot() + PLAT_WITH_DRAW);
        return mailConfig.getReportDownload() + PLAT_WITH_DRAW;
    }

    //导出管理员调整记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportBrokerAsset", method = RequestMethod.GET)
    public String exportBrokerAsset(@AuthForHeader AuthContext context,
                                  @RequestParam(value = "startDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                  @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                  @RequestParam(value = "assetCode", required = false) String assetCode,
                                  @RequestParam(value = "uid", required = false) Integer uid,
                                  @RequestParam(value = "email", required = false) String email) {
        List<ConfigAsset> availableAssetList = configAssetService.getAvailableAssetCode();
        Set<String> availableAssetSet = availableAssetList.stream().map(c -> c.getAssetCode())
                .collect(Collectors.toSet());
        if (assetCode != null && !availableAssetSet.contains(assetCode)) {
            throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
        }
        if(email != null){
            User user = userService.getUserByEmail(email);
            uid = user.getUid();
        }
        PageInfo<BrokerAssetOperDetail> pageInfo = brokerAssetOperDetailService.getBrokerAssetDetail(uid,assetCode,startDate,endDate,1, 10000);

        List<BrokerAssetDto> brokerAssetDtos = pageInfo.getList().stream().map(brokerAssetOperDetail -> {
            User user = userService.getUserByUid(brokerAssetOperDetail.getUid());
            return new BrokerAssetDto(brokerAssetOperDetail,user == null ?null:user.getEmail());
        }).collect(Collectors.toList());

        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(BrokerAssetDto brokerAssetDto : brokerAssetDtos){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate",brokerAssetDto.getCreateDate());
            resMap.put("assetCode",brokerAssetDto.getAssetCode());
            resMap.put("amount",brokerAssetDto.getAmount());
            resMap.put("uid",brokerAssetDto.getUid());
            resMap.put("oper_uid",brokerAssetDto.getOper_uid());
            resMap.put("account",brokerAssetDto.getAccount());
            resMap.put("remark",null);
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("brokerAsset.ftl",resultMerStlMap, mailConfig.getReportRoot() + BROKER_ASSET);
        return mailConfig.getReportDownload() + BROKER_ASSET;
    }
    //导出用户交易记录列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportUserTradeRecord", method = RequestMethod.GET)
    public String exportUserTradeRecord(@AuthForHeader AuthContext context,
                                  @RequestParam(value = "brokerId", required = false) Integer brokerId,
                                  @RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                  @RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                  @RequestParam(value = "uid", required = false) Integer uid,
                                  @RequestParam(value = "tradeFlag", required = false) TradeCoinFlag tradeFlag,
                                  @RequestParam(value = "symbol", required = false) String symbol,
                                  @RequestParam(value = "orderType", required = false) TradeCoinType orderType,
                                  @RequestParam(value = "status", required = false) TradeCoinStatus status) {
        PageInfo<TradeOrder> pageInfo = matchOrderService.getTradeOrderRecord(brokerId,tradeFlag,uid,symbol,orderType,status,beginTime,endTime,1, 10000);
        List<TradeOrderDto> tradeOrderDtos = pageInfo.getList().stream().map(tradeOrder -> new TradeOrderDto(tradeOrder)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(TradeOrderDto tradeOrderDto : tradeOrderDtos){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate",tradeOrderDto.getCreateDate());
            resMap.put("finishDate",tradeOrderDto.getFinishDate());
            resMap.put("symbol",tradeOrderDto.getSymbol());
            resMap.put("tradeType",tradeOrderDto.getTradeType());
            resMap.put("tradeFlag",tradeOrderDto.getTradeFlag());
            resMap.put("requestNo",tradeOrderDto.getRequestNo());
            resMap.put("uid",tradeOrderDto.getUid());
            resMap.put("number",tradeOrderDto.getNumber());
            resMap.put("price",tradeOrderDto.getPrice());
            resMap.put("tradedNumber",tradeOrderDto.getTradedNumber());
            resMap.put("numberOver",tradeOrderDto.getNumberOver());
            resMap.put("tradedMoney",tradeOrderDto.getTradedMoney());
            resMap.put("fee",tradeOrderDto.getFee());
            resMap.put("status",tradeOrderDto.getStatus());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("userTradeRecord.ftl",resultMerStlMap, mailConfig.getReportRoot() + USER_TRADE_RECORD);
        return mailConfig.getReportDownload() + USER_TRADE_RECORD;

    }

    //导出用户资产过程总表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportPlatAsset", method = RequestMethod.GET)
    public String exportPlatAsset(@AuthForHeader AuthContext context,
                                @RequestParam(value = "startDate", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                @RequestParam(value = "symbol", required = false) String symbol) {
        PageInfo<PlatAssetProcess> pageInfo = platAssetProessService.selectPlatAssetProcess(endDate,symbol,1,1000);
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(PlatAssetProcess process : pageInfo.getList()){
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("symbol",process.getAssetCode());
            resMap.put("beginBalance",process.getBeginBalance());
            resMap.put("depositBalance",process.getDepositBalance());
            resMap.put("withdraw",process.getWithdrawTotal());
            resMap.put("withdrawUnknow",process.getWithdrawUnknow());
            resMap.put("withdrawSuccess",process.getWithdrawSuccess());
            resMap.put("withdrawRefuse",process.getWithdrawRefuse());
            resMap.put("withdrawFee",process.getWithdrawFee());
            resMap.put("brokenAsset",process.getBrokenAssetBalance());
            resMap.put("tradeFee",process.getTradeFee());
            resMap.put("other",process.getOther());
            resMap.put("endBalance",process.getEndBalance());
            resMap.put("culBalance",process.getCulBalance());
            lst.add(resMap);
        }

        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platAssetProess.ftl",resultMerStlMap, mailConfig.getReportRoot() + PLAT_ASSET_PROCESS);
        return mailConfig.getReportDownload() + PLAT_ASSET_PROCESS;
    }

}
