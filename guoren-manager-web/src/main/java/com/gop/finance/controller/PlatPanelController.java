package com.gop.finance.controller;

import com.gop.asset.service.ConfigAssetService;
import com.gop.coin.transfer.service.DepositCoinOrderService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.domain.ConfigAsset;
import com.gop.domain.DepositCoinOrderUser;
import com.gop.domain.StatisticeResult;
import com.gop.finance.dto.AssetCoinDto;
import com.gop.finance.dto.UserStatisticsDto;
import com.gop.finance.service.UserStatisticsService;
import com.gop.match.service.TradeRecordService;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by wuyanjie on 2018/5/21.
 */
@RestController("PlatPanelController")
@RequestMapping("/panel")
@Slf4j
public class PlatPanelController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserStatisticsService userStatisticsService;
    @Autowired
    private TradeRecordService tradeRecordService;
    @Autowired
    private ConfigAssetService configAssetService;
    @Autowired
    private DepositCoinQueryService depositCoinQueryService;
    @Autowired
    private WithdrawCoinQueryService withdrawCoinQueryService;
    // 用户统计
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getUserStatistics", method = RequestMethod.GET)
    public List<UserStatisticsDto> getUserStatistics(@AuthForHeader AuthContext context) {
        return userStatisticsService.selectUserStatistics().stream()
                .map(userStatistics ->  new UserStatisticsDto(userStatistics)).collect(Collectors.toList());
    }

    // 交易统计（24h）
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getTradeStatistics", method = RequestMethod.GET)
    public List<StatisticeResult> getTradeStatistics(@AuthForHeader AuthContext context) {
        List<StatisticeResult> statisticeResults = tradeRecordService.getNumberBySymbol();
        return statisticeResults;
    }



    // 充值提现统计（24h）
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getCoinStatistics", method = RequestMethod.GET)
    public List<AssetCoinDto> getCoinStatistics(@AuthForHeader AuthContext context) {

        List<ConfigAsset> confs = configAssetService.getAvailableAssetCode();
        //充值统计
        List<StatisticeResult> depositStaitstic = depositCoinQueryService.depositStaitstic();
        Map<String,BigDecimal> depositMap = depositStaitstic.stream().collect(Collectors.toMap(StatisticeResult::getSymbol, StatisticeResult::getTradeNumber));

        //提现统计
        List<StatisticeResult> withdrawStatistic = withdrawCoinQueryService.withdrawStaitstic();
        Map<String,BigDecimal> withdrawMap = withdrawStatistic.stream().collect(Collectors.toMap(StatisticeResult::getSymbol, StatisticeResult::getTradeNumber));

        return confs.stream().map(configAsset -> {
            String assetCode = configAsset.getAssetCode();
            BigDecimal depositNumber = depositMap.get(assetCode)==null?BigDecimal.ZERO:depositMap.get(assetCode);
            BigDecimal withdrawNumber = withdrawMap.get(assetCode)==null?BigDecimal.ZERO:withdrawMap.get(assetCode);
            return new AssetCoinDto(configAsset.getAssetCode(),depositNumber,withdrawNumber);
        } ).collect(Collectors.toList());
    }

}

