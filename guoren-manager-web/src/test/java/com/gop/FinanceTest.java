package com.gop;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.authentication.service.UserIdentificationService;
import com.gop.authentication.service.UserResidenceService;
import com.gop.code.consts.ActivityCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.coin.transfer.dto.DepositDetailDto;
import com.gop.coin.transfer.dto.WithdrawDetailDto;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.domain.*;
import com.gop.domain.enums.*;
import com.gop.exception.AppException;
import com.gop.finance.dto.*;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.finance.service.PlatAssetProessService;
import com.gop.finance.service.UserStatisticsService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.TradeRecordService;
import com.gop.mode.vo.PageModel;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.IEmailService;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserLoginLogService;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wuyanjie on 2018/4/24.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GuorenManagerWebSpringbootApplication.class})
@Slf4j
public class FinanceTest {

    private static final String FILE_PATH=  "./data/";
    private static final String DOWNLOAD_PATH ="./data/";

    @Autowired
    private ConfigAssetService configAssetService;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private BrokerAssetOperDetailService brokerAssetOperDetailService;

    @Autowired
    private MatchOrderService matchOrderService;

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Test
    public void testUserAssetList() throws ParseException {
        Date date = new Date();
        Integer uid = null;
        String email = null;
        String assetCode = null;
        int pageNo =1;
        int pageSize = 10;
        PageModel<AssetDto> pageModel = new PageModel();
        PageInfo<UserBeginingBalance> pageInfo= beginingBalanceService.getUserBeginingBalance(uid,email,assetCode,date,pageNo,pageSize);
        pageModel.setList(pageInfo.getList().stream().map(userBeginingBalance -> new AssetDto(userBeginingBalance)).collect(Collectors.toList()));
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        System.out.println("");
    }

    @Test
    public void testPlatAccount() throws ParseException {
        Integer uid = null;
        String email = "";
        String assetCode = null;
        Integer pageNo = 1;
        Integer pageSize = 10;
        Date date = null;
        if (assetCode != null && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
            throw new AppException(ActivityCodeConst.FIELD_ERROR, "无效币种");
        }
        PageModel<PlatAssetDto> model = new PageModel();
        if(date != null && !org.apache.commons.lang.time.DateUtils.isSameDay(date,new Date())){
            PageInfo<BeginingBalance> pageInfo = beginingBalanceService.queryBeginingBalance(assetCode,date,pageNo,pageSize);
            model.setList(pageInfo.getList().stream().map(beginingBalance -> new PlatAssetDto(beginingBalance)).collect(Collectors.toList()));
            model.setPageNo(pageNo);
            model.setPageSize(pageSize);
            model.setPageNum(pageInfo.getPages());
            model.setTotal(pageInfo.getTotal());
        }else {
            PageInfo<FinanceAmountDto> pageInfo = financeService.getTotalAccountByAssetCode(assetCode, pageNo, pageSize);
            model.setList(pageInfo.getList().stream().map(finance -> new PlatAssetDto(finance)).collect(Collectors.toList()));
            model.setPageNo(pageNo);
            model.setPageSize(pageSize);
            model.setPageNum(pageInfo.getPages());
            model.setTotal(pageInfo.getTotal());
        }
        System.out.println("");
    }


    //管理员调整记录
    @Test
    public void testGetBrokenAsset() throws ParseException {
        String assetCode = "BTC";
        String email = null;
        Integer uid = null;
        Date startDate = null;
        Date endDate = null;
        Integer pageNo = 1;
        Integer pageSize = 10;
        List<ConfigAsset> availableAssetList = configAssetService.getAvailableAssetCode();
        Set<String> availableAssetSet = availableAssetList.stream().map(c -> c.getAssetCode())
                .collect(Collectors.toSet());
        if (assetCode != null && !availableAssetSet.contains(assetCode)) {
            throw new AppException(ActivityCodeConst.FIELD_ERROR, "无效币种");
        }
        if (uid != null && email != null) {
            throw new AppException(CommonCodeConst.FIELD_ERROR);
        }
        if (email != null) {
            User user = userService.getUserByEmail(email);
            uid = user.getUid();
        }
        PageInfo<BrokerAssetOperDetail> pageInfo = brokerAssetOperDetailService.getBrokerAssetDetail(uid, assetCode, startDate, endDate, pageNo, pageSize);
        PageModel<BrokerAssetDto> pageModel = new PageModel<>();

        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setList(
                pageInfo.getList().stream().map(brokerAssetOperDetail -> new BrokerAssetDto(
                        brokerAssetOperDetail,
                        userService.getUserByUid(brokerAssetOperDetail.getUid()).getEmail()
                )).collect(Collectors.toList()));

    }


    //用户交易记录
    @Test
    public void testGetUserTradeRecord() throws ParseException {
        Integer brokerId = 10003;
        TradeCoinFlag tradeFlag = null;
        Integer uid = null;
//        String symbol = "ACT_KCASH";
        String symbol = null;
        TradeCoinType orderType = null;
        TradeCoinStatus status = null;
        Date startDate = null;
        Date endDate = null;
        Integer pageNo = 1;
        Integer pageSize = 10;
        PageInfo<TradeOrder> pageInfo = matchOrderService.getTradeOrderRecord(brokerId, tradeFlag, uid, symbol, orderType, status, startDate, endDate, pageNo, pageSize);
        PageModel<TradeOrderDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setList(
                pageInfo.getList().stream().map(tradeOrder -> new TradeOrderDto(tradeOrder)).collect(Collectors.toList()));

    }

    @Test
    public void testExportUserAsset() {
        String email = null;
        Integer uid = 14;
        String assetCode = "BTC";
        if (email != null) {
            User user = userFacade.getUser(email);
            uid = user.getUid();
        }
        if (assetCode != null && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
            throw new AppException(CommonCodeConst.FIELD_ERROR);
        }
        List<User> users = userService.getUsers(null);
        Map<Integer,String> maps = Maps.newHashMap();
        for(User user : users){
            maps.put(user.getUid(),user.getEmail());
        }
        List<AssetDto> assetDtos = financeService.getUserAccountList(uid, assetCode).stream().map(finance -> new AssetDto(
                finance,
                maps.get(finance.getUid())
        )).collect(Collectors.toList());

        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        for (AssetDto assetDto : assetDtos) {
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("UID", assetDto.getUserId());
            resMap.put("email", assetDto.getAccountNo());
            resMap.put("assetCode", assetDto.getAssetCode());
            resMap.put("amountAvailable", assetDto.getAmountAvailable());
            resMap.put("amountLock", assetDto.getAmountLock());
            resMap.put("amountTotal", assetDto.getAmountTotal());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        String xlsPath = exportExcelService.createTemplateXlsByFileName("userAsset.ftl", resultMerStlMap, "./userAsset" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls");

    }

    @Autowired
    private BeginingBalanceService beginingBalanceService;

    @Autowired
    private DepositCoinQueryService depositCoinQueryService;
    @Autowired
    private WithdrawCoinQueryService withdrawCoinQueryService;
    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private PlatAssetProessService platAssetProessService;
    @Test
    public void testAllPlatAsset() {
        String symbol =null;
        Integer pageNo = 1;
        Integer pageSize = 10;
        Date startDate = null;
        Date endDate = new Date();
        PageInfo<PlatAssetProcess> pageInfo = platAssetProessService.selectPlatAssetProcess(endDate,symbol,pageNo,pageSize);
        PageModel<PlatAssetProcessDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setList(pageInfo.getList().stream().map(platAssetProcess -> new PlatAssetProcessDto(platAssetProcess)).collect(Collectors.toList()));
        System.out.print("");
    }

    @Test
    public void testExportPlatAccounts() {
        String assetCode = "BTC";
        String fileName = null;
        String filePath = null;
        // 币种校验
        if (assetCode != null && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
            throw new AppException(ActivityCodeConst.FIELD_ERROR, "无效币种");
        }
        List<FinanceAmountDto> financeAmountDtos = financeService.getTotalAccountNoPage(assetCode);
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
        if (StringUtils.isEmpty(fileName)) {
            fileName = "platAssetStatus" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
        }
        if (StringUtils.isEmpty(filePath)) {
            filePath = "./data/";
        }
        exportExcelService.createTemplateXlsByFileName("platAssetStatus.ftl", resultMerStlMap, filePath + fileName + ".xls");
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


    @Test
    public void exportDeposit() {
        String email = null;
        Integer brokerId = null;
        String address = null;
        Integer uid = null;
        String assetCode = null;
        Date startDate = null;
        Date endDate = null;
        String fileName = null;
        String filePath = null;
        PageInfo<DepositCoinOrderUser> pageInfo = depositCoinQueryService.queryOrder(brokerId, null, email, uid,
                address, null, assetCode, startDate, endDate, email, null, 1, 10000);
        List<DepositDetailDto> depositDetailDtos = pageInfo.getList().stream().map(order -> new DepositDetailDto(order)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        for (DepositDetailDto depositDetailDto : depositDetailDtos) {
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate", depositDetailDto.getCreateDate());
            resMap.put("uid", depositDetailDto.getUid());
            resMap.put("account", depositDetailDto.getAccount());
            resMap.put("assetCode", depositDetailDto.getAssetCode());
            resMap.put("address", depositDetailDto.getAddress());
            resMap.put("amount", depositDetailDto.getAmount());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        if (StringUtils.isEmpty(fileName)) {
            fileName = "platDeposit" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
        }
        if (StringUtils.isEmpty(filePath)) {
            filePath = FILE_PATH;
        }
        exportExcelService.createTemplateXlsByFileName("platDeposit.ftl", resultMerStlMap, filePath + fileName + ".xls");

    }

    @Test
    public void testExportWithdraw() {
        String email = null;
        Integer brokerId = null;
        String address = null;
        Integer uId = null;
        String assetCode = "BTC";
        Date beginTime = null;
        Date endTime = null;
        String fileName = null;
        String filePath = null;
        WithdrawCoinOrderStatus status = WithdrawCoinOrderStatus.SUCCESS;
        PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryOrder(brokerId, null, email, uId,
                address, null, assetCode, beginTime, endTime, email, status, 1, 10000);
        List<WithdrawDetailDto> withdrawCoinOrderUsers = pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        for (WithdrawDetailDto withdrawDetailDto : withdrawCoinOrderUsers) {
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate", withdrawDetailDto.getCreateDate());
            resMap.put("updateDate", withdrawDetailDto.getUpdateDate());
            resMap.put("status", withdrawDetailDto.getStatus());
            resMap.put("uid", withdrawDetailDto.getUid());
            resMap.put("account", withdrawDetailDto.getAccount());
            resMap.put("assetCode", withdrawDetailDto.getAssetCode());
            resMap.put("address", withdrawDetailDto.getAddress());
            resMap.put("amount", withdrawDetailDto.getAmount());
            resMap.put("txFee", withdrawDetailDto.getTxFee());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        if (StringUtils.isEmpty(fileName)) {
            fileName = "platWithdraw" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
        }
        if (StringUtils.isEmpty(filePath)) {
            filePath = FILE_PATH;
        }
        exportExcelService.createTemplateXlsByFileName("platWithdraw.ftl", resultMerStlMap, filePath + fileName + ".xls");

    }

    @Test
    public void exportBrokerAsset() {
        String email = null;
        Integer uid = null;
        String assetCode = "BTC";
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = DateUtils.parseDate("2018-04-03 00:00:00");
            endDate = DateUtils.parseDate("2018-05-23 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String fileName = null;
        String filePath = null;
        List<ConfigAsset> availableAssetList = configAssetService.getAvailableAssetCode();
        Set<String> availableAssetSet = availableAssetList.stream().map(c -> c.getAssetCode())
                .collect(Collectors.toSet());
        if (assetCode != null && !availableAssetSet.contains(assetCode)) {
            throw new AppException(ActivityCodeConst.FIELD_ERROR, "无效币种");
        }
        if (email != null) {
            User user = userService.getUserByEmail(email);
            uid = user.getUid();
        }
        PageInfo<BrokerAssetOperDetail> pageInfo = brokerAssetOperDetailService.getBrokerAssetDetail(uid, assetCode, startDate, endDate, 1, 10000);

        List<BrokerAssetDto> brokerAssetDtos = pageInfo.getList().stream().map(brokerAssetOperDetail -> {
            User user = userService.getUserByUid(brokerAssetOperDetail.getUid());
            return new BrokerAssetDto(brokerAssetOperDetail, user == null ? null : user.getEmail());
        }).collect(Collectors.toList());

        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        for (BrokerAssetDto brokerAssetDto : brokerAssetDtos) {
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate", brokerAssetDto.getCreateDate());
            resMap.put("assetCode", brokerAssetDto.getAssetCode());
            resMap.put("amount", brokerAssetDto.getAmount());
            resMap.put("uid", brokerAssetDto.getUid());
            resMap.put("oper_uid", brokerAssetDto.getOper_uid());
            resMap.put("account", brokerAssetDto.getAccount());
            resMap.put("remark", null);
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        if (StringUtils.isEmpty(fileName)) {
            fileName = "brokerAsset" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
        }
        if (StringUtils.isEmpty(filePath)) {
            filePath = FILE_PATH;
        }
        exportExcelService.createTemplateXlsByFileName("brokerAsset.ftl", resultMerStlMap, filePath + fileName + ".xls");

    }

    @Test
    public void exportUserTradeRecord() {
        TradeCoinType orderType = null;
        Integer brokerId = null;
        TradeCoinFlag tradeFlag = null;
        Integer uid = null;
        String symbol = null;
        Date beginTime = null;
        Date endTime = null;
        String fileName = null;
        String filePath = null;
        TradeCoinStatus status = null;
        PageInfo<TradeOrder> pageInfo = matchOrderService.getTradeOrderRecord(brokerId, tradeFlag, uid, symbol, orderType, status, beginTime, endTime, 1, 999999);
        List<TradeOrderDto> tradeOrderDtos = pageInfo.getList().stream().map(tradeOrder -> new TradeOrderDto(tradeOrder)).collect(Collectors.toList());
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        for (TradeOrderDto tradeOrderDto : tradeOrderDtos) {
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("createDate", tradeOrderDto.getCreateDate());
            resMap.put("finishDate", tradeOrderDto.getFinishDate() == null ? null : tradeOrderDto.getFinishDate());
            resMap.put("symbol", tradeOrderDto.getSymbol());
            resMap.put("tradeType", tradeOrderDto.getTradeType());
            resMap.put("tradeFlag", tradeOrderDto.getTradeFlag());
            resMap.put("requestNo", tradeOrderDto.getRequestNo());
            resMap.put("uid", tradeOrderDto.getUid());
            resMap.put("number", tradeOrderDto.getNumber());
            resMap.put("price", tradeOrderDto.getPrice());
            resMap.put("tradedNumber", tradeOrderDto.getTradedNumber());
            resMap.put("numberOver", tradeOrderDto.getNumberOver());
            resMap.put("tradedMoney", tradeOrderDto.getTradedMoney());
            resMap.put("fee", tradeOrderDto.getFee());
            resMap.put("status", tradeOrderDto.getStatus());
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        if (StringUtils.isEmpty(fileName)) {
            fileName = "userTradeRecord" + DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
        }
        if (StringUtils.isEmpty(filePath)) {
            filePath = FILE_PATH;
        }
        exportExcelService.createTemplateXlsByFileName("userTradeRecord.ftl", resultMerStlMap, "./data/" + fileName + ".xls");

    }

    @Test
    public void exportPlatAsset() {
        String symbol = "BTC";
        String fileName = null;
        String filePath = null;
        TradeCoinStatus status = null;
        Date startDate = null;
        try {
            startDate = DateUtils.parseDate("2018-05-01 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = null;
        if(symbol != null && !configAssetService.validateAssetConfig(symbol, AssetStatus.LISTED)){
            throw new AppException(CommonCodeConst.FIELD_ERROR);
        }
        //查询币种
        List<ConfigAsset> configAssets = configAssetService.getAllAssetCodeByAssetCode(symbol);
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        //获取期初金额
        Map<String,BigDecimal> beginBalanceMaps =beginingBalanceService.getBeginingBalance(symbol, startDate).stream()
                .collect(Collectors.toMap(BeginingBalance::getAssetCode,BeginingBalance::getAmount_total));
        //查询转入总额
        Map<String,BigDecimal> depositMaps = depositCoinQueryService.getTotalDeposits(symbol,DepositCoinAssetStatus.SUCCESS,startDate,endDate)
                .stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

        //查询提现相关金额
        Map<String,Map<String,BigDecimal>> withdrawMap = withdrawCoinQueryService.getTotalWithdrawedCoinValue(symbol,startDate,endDate);
        Map<String,BigDecimal> numberMaps = withdrawMap.get("number");
        Map<String,BigDecimal> txfeeMaps = withdrawMap.get("txfee");

        //管理员调整资产
        Map<String,BigDecimal> brokerMaps =brokerAssetOperDetailService.getBroketAssetByAsset(symbol, startDate,endDate).stream()
                .collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

        //交易手续费
        Map<String,BigDecimal> buyFees = tradeRecordService.culBuyFeeByAsset(symbol,startDate,endDate);
        Map<String,BigDecimal> sellFees = tradeRecordService.culSellFeeByAsset(symbol,startDate,endDate);
        //期末余额
        Map<String,BigDecimal> financeMap = financeService.queryTotalCountByAssetCode(symbol)
                .stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));

        for(ConfigAsset configAsset : configAssets){
            String asset = configAsset.getAssetCode();
            //获取期初金额
            BigDecimal beginingBalance = beginBalanceMaps.get(asset)==null ? BigDecimal.ZERO:beginBalanceMaps.get(asset);

            //查询转入总额
            BigDecimal depositAmount = depositMaps.get(asset) ==null ? BigDecimal.ZERO:depositMaps.get(asset);

            //管理员调整资产
            BigDecimal brokerAsset = brokerMaps.get(asset)==null?BigDecimal.ZERO:brokerMaps.get(asset);

            //交易手续费
            BigDecimal buyFee = buyFees.get(asset)==null?BigDecimal.ZERO:buyFees.get(asset);
            BigDecimal sellFee = sellFees.get(asset)==null?BigDecimal.ZERO:sellFees.get(asset);
            BigDecimal tradeFee = buyFee.add(sellFee);

            //其他
            BigDecimal otherAmount = BigDecimal.ZERO;
            //期末余额
            BigDecimal endBalance =financeMap.get(asset) == null ? BigDecimal.ZERO :financeMap.get(asset);
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
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platAssetProess.ftl", resultMerStlMap, "./data/"+ "platAssetProess.xls");
    }
    private static final String STATUS_PATH_NAME = "./data/platAssetStatus"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String PROESS_PATH_NAME = "./data/platAssetProess"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    @Autowired
    private IEmailService iEmailService;

    @Value("${mail.to-user}")
    private String toUser;
    @Value("${mail.report-subject}")
    private String subject;
    @Value("${mail.report-text}")
    private String text;

    /**
     * 测试带附件的发送邮件
     */
    @Test
    public void sendAttachmentsEmail(){
//        exportPlatAsset();
        List<File> files = Lists.newArrayList(new File("userBaseList.xls"));
        //发送邮件
        EmailDto email = EmailDto.builder().toUser(Arrays.asList(toUser.split(","))).subject(subject).fileList(files).text(text).build();
        Boolean isSent = iEmailService.sendAttachmentsMail(email);
    }
    /**
     * 测试内容的发送邮件
     */
    @Test
    public void sendEmail(){
    	sendMailDirect("hfjinsong@163.com", "hello, email");
    }
    public void sendMailDirect(String toMail, String msgContent) {
		EmailDto emailDto = new EmailDto();
		emailDto.setSubject(subject);
		emailDto.setText(msgContent);

		emailDto.setToUser(Lists.newArrayList(toMail));
		iEmailService.sendSimpleMail(emailDto);

	}
    private static final String STATUS_USRE_PATH_NAME = "/data/app/multi/gdae2-exchange-manager/data/userAsset"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";

    @Autowired
    private UserIdentificationService userIdentificationService;
    @Autowired
    private UserResidenceService userResidenceService;
    @Test
    public void taskTest(){
        Date date = null;
        try {
            date = DateUtils.parseDate("2018-03-14 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //每天凌晨跑的是前一天的数据，记录日期为前一天
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
    @Autowired
    private UserLoginLogService userLoginLogService;
    @Test
    public void testGetAccount(){// 校验用户资产并添加新资产
        Integer uid = 2;
        // 校验用户资产并添加新资产
        Set<String> configSet = configAssetService.getAvailableAssetCode().stream().map(a -> a.getAssetCode())
                .collect(Collectors.toSet());
        List<Finance> lst = financeService.queryAccounts(uid);
        if (lst == null) {
            lst = Collections.emptyList();
        }
        Set<String> userAccountSet = lst.stream()
                .map(a -> configSet.contains(a.getAssetCode()) ? a.getAssetCode() : null).collect(Collectors.toSet());
        configSet.removeAll(userAccountSet);
        String accountKind = "MASTER";
        int brokerId = 10003;
        if (configSet.size() != 0) {
            for (String assetCode : configSet) {
                financeService.createFinanceAccount(uid, brokerId, assetCode, accountKind);
            }
        }
        Map<String,BigDecimal> maps = withdrawCoinQueryService.getSuccessWithdrawedCoin(uid).stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));
        List<StatisticeResult> statisticeResults = tradeRecordService.culTradeAmountByUid(uid);
        Map<String,BigDecimal> tradeMaps = Maps.newHashMap();
        for(StatisticeResult result : statisticeResults){
            String[] asset = result.getSymbol().split("_");
            if(tradeMaps.containsKey(asset[0])){
                tradeMaps.put(asset[0], tradeMaps.get(asset[0]).add(result.getTradeNumber()));
            }else{
                tradeMaps.put(asset[0],result.getTradeNumber());
            }
            if(tradeMaps.containsKey(asset[1])) {
                tradeMaps.put(asset[1], tradeMaps.get(asset[1]).add(result.getTradeNumber()));
            }else{
                tradeMaps.put(asset[1],result.getTradeNumber());
            }
        }
        financeService.queryAccounts(uid).stream().map(finance -> new com.gop.asset.dto.AssetDto(
                finance,
                tradeMaps.get(finance.getAssetCode()),
                maps.get(finance.getAssetCode())
        )).collect(Collectors.toList());
        System.out.print(DOWNLOAD_PATH + "userBaseList.xls");
    }
}