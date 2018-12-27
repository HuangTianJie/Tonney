package com.gte.asset.controller;

import com.gte.asset.dto.AssetDto;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.domain.Finance;
import com.gop.match.service.TradeRecordService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController("managerUserAsset")
@RequestMapping("/asset")
@Slf4j
// @Api("用户中心")
public class UserAssetController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private ConfigAssetService configAssetService;

    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private WithdrawCoinQueryService withdrawCoinQueryService;

    // 查询用户资产列表
    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
        @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getUserAccounts", method = RequestMethod.GET)
    public List<AssetDto> getUserAccountAssets(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid) {
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
//        Map<String,BigDecimal> maps = withdrawCoinQueryService.getSuccessWithdrawedCoin(uid).stream().collect(Collectors.toMap(StatisticeResult::getSymbol,StatisticeResult::getTradeNumber));
//        List<StatisticeResult> statisticeResults = tradeRecordService.culTradeAmountByUid(uid);
//        Map<String,BigDecimal> tradeMaps = Maps.newHashMap();
//        for(StatisticeResult result : statisticeResults){
//            String[] asset = result.getSymbol().split("_");
//            if(tradeMaps.containsKey(asset[0])){
//                tradeMaps.put(asset[0], tradeMaps.get(asset[0]).add(result.getTradeNumber()));
//            }else{
//                tradeMaps.put(asset[0],result.getTradeNumber());
//            }
//            if(tradeMaps.containsKey(asset[1])) {
//                tradeMaps.put(asset[1], tradeMaps.get(asset[1]).add(result.getTradeNumber()));
//            }else{
//                tradeMaps.put(asset[1],result.getTradeNumber());
//            }
//        }
        return financeService.queryAccounts(uid).stream().map(finance -> new AssetDto(
                finance,
//                tradeMaps.get(finance.getAssetCode()),
//                maps.get(finance.getAssetCode())
                BigDecimal.ZERO,BigDecimal.ZERO
        )).collect(Collectors.toList());
    }

//	@Autowired
//	private UserAccountFacade userAccountFacade;
//
//	@Strategys(strategys = { @Strategy(authStrategy = { "checkLoginStrategy","checkLoginPasswordStregy" }) })
//	@RequestMapping(value = "/deposit", method = RequestMethod.GET)
//	public void deposit(HttpSession session, @RequestParam("uid") Integer uid,@RequestParam("assetCode") String assetCode ,@RequestParam("amouont") BigDecimal amount) {
//		
//		String txid = OrderUtil.generateCode(OrderUtil.ORDER_SERVICE, OrderUtil.TRANSFER_IN_CURRENCY);
//		List<AssetOperationDto> ops = new ArrayList<>();
//		
//		AssetOperationDto depositDto = new AssetOperationDto();
//		depositDto.setAccountClass(AccountClass.LIABILITY);
//		depositDto.setAccountSubject(AccountSubject.DEPOSIT_COMMON);
//		depositDto.setAssetCode(assetCode);
//		depositDto.setBusinessSubject(BusinessSubject.DEPOSIT);
//		depositDto.setAmount(amount);
//		depositDto.setLoanAmount(BigDecimal.ZERO);
//		depositDto.setLockAmount(BigDecimal.ZERO);
//		depositDto.setMemo("券商充值");
//		depositDto.setRequestNo(txid);
//		depositDto.setUid(uid);
//		ops.add(depositDto);
//		
//		try {
//			userAccountFacade.assetOperation(ops);
//		} catch (Exception e) {
//			log.info("退款给用户失败:{}", e.getMessage());
//			throw e;
//		}
//		userAccountFacade.assetOperation(ops);
//	}
//	
//	
//	
//	@Strategys(strategys = { @Strategy(authStrategy = { "checkLoginStrategy","checkLoginPasswordStregy" }) })
//	@RequestMapping(value = "/withdraw", method = RequestMethod.GET)
//	public void withdraw(HttpSession session, @RequestParam("uid") Integer uid,@RequestParam("assetCode") String assetCode ,@RequestParam("amouont") BigDecimal amount) {
//		
//		String txid = OrderUtil.generateCode(OrderUtil.ORDER_SERVICE, OrderUtil.TRANSFER_IN_CURRENCY);
//		List<AssetOperationDto> ops = new ArrayList<>();
//		
//		AssetOperationDto depositDto = new AssetOperationDto();
//		depositDto.setAccountClass(AccountClass.LIABILITY);
//		depositDto.setAccountSubject(AccountSubject.WITHDRAW_COMMON);
//		depositDto.setAssetCode(assetCode);
//		depositDto.setBusinessSubject(BusinessSubject.WITHDRAW);
//		depositDto.setAmount(BigDecimal.ZERO.subtract(amount));
//		depositDto.setLoanAmount(BigDecimal.ZERO);
//		depositDto.setLockAmount(BigDecimal.ZERO);
//		depositDto.setMemo("券商提现");
//		depositDto.setRequestNo(txid);
//		depositDto.setUid(uid);
//		ops.add(depositDto);
//		
//		try {
//			userAccountFacade.assetOperation(ops);
//		} catch (Exception e) {
//			log.info("退款给用户失败:{}", e.getMessage());
//			throw e;
//		}
//		userAccountFacade.assetOperation(ops);
//	}

}
