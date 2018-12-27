package com.gop.finance.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.ActivityCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.domain.*;
import com.gop.domain.enums.AssetStatus;
import com.gop.exception.AppException;
import com.gop.finance.dto.AssetDto;
import com.gop.finance.dto.PlatAssetDto;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wuyanjie on 2018/4/23.
 */
@RestController("InternalAssetsStatusController")
@RequestMapping("/internalAssetStatus")
@Slf4j
@EnableConfigurationProperties(MailConfig.class)
public class InternalAssetsStatusController {
    private static final String USER_ASSET="userAsset.xls";
    private static final String PLAT_ASSET_STATUS="platAssetStatus.xls";

    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private ConfigAssetService configAssetService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private BeginingBalanceService beginingBalanceService;
    // 查询用户资产状态列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getUserAccounts", method = RequestMethod.GET)
    public PageModel<AssetDto> getUserAccountAssets(@AuthForHeader AuthContext context,
                                                    @RequestParam(value = "uid", required = false) Integer uid,
                                                    @RequestParam(value = "email", required = false) String email,
                                                    @RequestParam(value = "assetCode", required = false) String assetCode,
                                                    @RequestParam(value = "date", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                    @RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {

        PageModel<AssetDto> pageModel = new PageModel();
//        if(date != null && !DateUtils.isSameDay(date,new Date())){
            PageInfo<UserBeginingBalance> pageInfo= beginingBalanceService.getUserBeginingBalance(uid,email,assetCode,date,pageNo,pageSize);
            pageModel.setList(pageInfo.getList().stream().map(userBeginingBalance -> new AssetDto(userBeginingBalance)).collect(Collectors.toList()));
            pageModel.setPageNum(pageInfo.getPages());
            pageModel.setTotal(pageInfo.getTotal());
//        }else {
//            // 校验用户资产并添加新资产
//            Set<String> initConfigSet = assetCode == null ? configAssetService.getAvailableAssetCode().stream().filter(a -> assetCode == null || assetCode.equals(a.getAssetCode())).map(a -> a.getAssetCode())
//                    .collect(Collectors.toSet()) : Sets.newHashSet(assetCode);
//
//            if (email != null) {
//                User user = userFacade.getUser(email);
//                uid = user.getUid();
//            }
//            List<User> userList = userService.getUsers(uid);
//            for (User user : userList) {
//                Integer uuid = user.getUid();
//                Set<String> configSet = initConfigSet;
//                List<Finance> lst = financeService.queryAccounts(uuid);
//                if (lst == null) {
//                    lst = Collections.emptyList();
//                }
//                Set<String> userAccountSet = lst.stream()
//                        .map(a -> configSet.contains(a.getAssetCode()) ? a.getAssetCode() : null).collect(Collectors.toSet());
//                configSet.removeAll(userAccountSet);
//                String accountKind = "MASTER";
//                int brokerId = 10003;
//                if (configSet.size() != 0) {
//                    for (String code : configSet) {
//                        financeService.createFinanceAccount(uuid, brokerId, code, accountKind);
//                    }
//                }
//            }
//            List<User> users = userService.getUsers(null);
//            Map<Integer,String> maps = Maps.newHashMap();
//            for(User user : users){
//                maps.put(user.getUid(),user.getEmail());
//            }
//            PageInfo<Finance> pageInfo = financeService.selectUserAccountList(uid, assetCode, pageNo, pageSize);
//            pageModel.setList(pageInfo.getList().stream().map(finance -> new AssetDto(
//                    finance,
//                    maps.get(finance.getUid())
//            )).collect(Collectors.toList()));
//            pageModel.setPageNum(pageInfo.getPages());
//            pageModel.setTotal(pageInfo.getTotal());
//        }
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        return pageModel;
    }

    // 查询平台资产状态总列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getPlatAccounts", method = RequestMethod.GET)
    public PageModel<PlatAssetDto> getPlatAccounts(@AuthForHeader AuthContext context,
                                                   @RequestParam(value = "assetCode", required = false) String assetCode,
                                                   @RequestParam(value = "date", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam("pageSize") Integer pageSize) {
        if (assetCode != null && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
            throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
        }
        PageModel<PlatAssetDto> pageModel = new PageModel<>();
        if(date != null){
            PageInfo<BeginingBalance> pageInfo = beginingBalanceService.queryBeginingBalance(assetCode,date,pageNo,pageSize);
            pageModel.setPageNo(pageNo);
            pageModel.setPageSize(pageSize);
            pageModel.setPageNum(pageInfo.getPages());
            pageModel.setTotal(pageInfo.getTotal());
            pageModel.setList(pageInfo.getList().stream().map(beginingBalance -> new PlatAssetDto(beginingBalance)).collect(Collectors.toList()));
            return pageModel;
        }else {
            PageInfo<FinanceAmountDto> pageInfo = financeService.getTotalAccountByAssetCode(assetCode, pageNo, pageSize);
            pageModel.setPageNo(pageNo);
            pageModel.setPageSize(pageSize);
            pageModel.setPageNum(pageInfo.getPages());
            pageModel.setTotal(pageInfo.getTotal());
            pageModel.setList(pageInfo.getList().stream().map(financeAmountDto -> new PlatAssetDto(financeAmountDto)).collect(Collectors.toList()));
            return pageModel;
        }
    }

    //导出用户资产状态报表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/exportUserAsset", method = RequestMethod.GET)
    public String exportUserAsset(@AuthForHeader AuthContext context,
                                  @RequestParam(value = "uid", required = false) Integer uid,
                                  @RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "assetCode", required = false) String assetCode,
                                  @RequestParam(value = "date", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {

        List<AssetDto> assetDtos = Lists.newArrayList();
//        if(date != null &&  !DateUtils.isSameDay(date,new Date())){
            assetDtos= beginingBalanceService.getUserBeginingBalance(uid,email,assetCode,date,1,10000).getList()
                    .stream().map(userBeginingBalance -> new AssetDto(userBeginingBalance)).collect(Collectors.toList());
//        }else {
//            if (email != null) {
//                User user = userFacade.getUser(email);
//                uid = user.getUid();
//            }
//            List<User> users = userService.getUsers(null);
//            Map<Integer,String> maps = Maps.newHashMap();
//            for(User user : users){
//                maps.put(user.getUid(),user.getEmail());
//            }
//            assetDtos = financeService.getUserAccountList(uid, assetCode).stream().map(finance -> new AssetDto(
//                    finance,
//                    maps.get(finance.getUid())
//            )).collect(Collectors.toList());
//        }
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
        exportExcelService.createTemplateXlsByFileName("userAsset.ftl",resultMerStlMap, mailConfig.getReportRoot() + USER_ASSET);
        return mailConfig.getReportDownload()+USER_ASSET;
    }


    // 导出平台资产状态总列表
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/exportPlatAccounts", method = RequestMethod.GET)
    public String exportPlatAccounts(@AuthForHeader AuthContext context,
                                     @RequestParam(value = "assetCode", required = false) String assetCode,
                                     @RequestParam(value = "date", required = true)  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        if (assetCode != null && !configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED)) {
            throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
        }
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
//        if(date != null && !DateUtils.isSameDay(date,new Date())){
            List<BeginingBalance> beginingBalances = beginingBalanceService.getBeginingBalance(assetCode,date);
            for (BeginingBalance beginingBalance : beginingBalances) {
                Map<String, Object> resMap = new HashMap<String, Object>();
                resMap.put("assetCode", beginingBalance.getAssetCode());
                resMap.put("amountAvailable", beginingBalance.getAmount_available());
                resMap.put("amountLock", beginingBalance.getAmount_lock());
                resMap.put("amountTotal", beginingBalance.getAmount_total());
                lst.add(resMap);
            }
//        }else {
//            List<FinanceAmountDto> financeAmountDtos = financeService.getTotalAccountNoPage(assetCode);
//            for (FinanceAmountDto financeAmountDto : financeAmountDtos) {
//                Map<String, Object> resMap = new HashMap<String, Object>();
//                resMap.put("assetCode", financeAmountDto.getAssetCode());
//                resMap.put("amountAvailable", financeAmountDto.getAmountAvailable());
//                resMap.put("amountLock", financeAmountDto.getAmountLock());
//                resMap.put("amountTotal", financeAmountDto.getAmountTotal());
//                lst.add(resMap);
//            }
//        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName("platAssetStatus.ftl",resultMerStlMap, mailConfig.getReportRoot() + PLAT_ASSET_STATUS);
        return mailConfig.getReportDownload() + PLAT_ASSET_STATUS;
    }
}
