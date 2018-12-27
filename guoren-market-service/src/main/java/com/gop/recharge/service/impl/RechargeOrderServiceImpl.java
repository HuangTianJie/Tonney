package com.gop.recharge.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.domain.RechargeOrder;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.RechargeOrderMapper;
import com.gop.recharge.service.RechargeOrderService;
import com.gop.util.MD5Util;
import com.gop.util.SequenceUtil;

@Service("RechargeOrderService")
@Slf4j
public class RechargeOrderServiceImpl implements RechargeOrderService {

    @Autowired
    private RechargeOrderMapper rechargeOrderMapper;

    @Autowired
    private SzfChargeService szfChargeService;

    @Autowired
    private UserAccountFacade userAccountFacade;

    /**
     *
     */
    @Override
    public String doRecharge(int uscwalletUserId, int userId, Long payMoney, String asset, String payChannel,
            int payType, String merId, String privateKey, String desKey, String postUrl, String cardNum, String cardInfo,String returnUrl) {
        if (asset != "USC") {
            return "404";
        }
        String coinType = "USC";
        UserAccountDto userAccount = this.userAccountFacade.queryAccount(uscwalletUserId, coinType);
        log.info("USC 储备金账号:{},余额:{}", uscwalletUserId, userAccount.getAmountAvailable()
            .doubleValue());
        if (userAccount.getAmountAvailable().doubleValue() < 10) {
            log.info("USC准备金账户余额不足:available:{}", userAccount.getAmountAvailable()
                .doubleValue());
            return "404";
        }
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setAsset("USC");
        rechargeOrder.setAssetAmount(new BigDecimal(payMoney).multiply(new BigDecimal("0.975")).divide(new BigDecimal(669), 3, BigDecimal.ROUND_DOWN));
        rechargeOrder.setCreateTime(new Date());
        rechargeOrder.setExtraInfo(cardNum);
        rechargeOrder.setFee(0L);
        rechargeOrder.setPayChannel("SZF");
        rechargeOrder.setPayMoney(payMoney);
        rechargeOrder.setPayType(payType);
        rechargeOrder.setStatus(2);//处理中
        rechargeOrder.setUserId(userId);
        this.rechargeOrderMapper.insert(rechargeOrder);
        String resultCode = null;
        try {
            resultCode = this.szfChargeService.createOrder(rechargeOrder.getId(), rechargeOrder.getPayMoney(),
                cardInfo, rechargeOrder.getPayType(), merId, privateKey, desKey, postUrl,returnUrl);
        } catch (Exception e) {

        }
        return resultCode;
    }

    @Override
    public void doCallback(int uscwalletUserId, String merId, long payMoney, long cardMoney, String orderId,
            int payResult, String privateField, String privateKey, String md5String) {

        boolean paySuccess = false;
        RechargeOrder rechargeOrder = this.rechargeOrderMapper.selectByPrimaryKey(Integer.valueOf(orderId));
        if (rechargeOrder == null || rechargeOrder.getStatus() != 2) {
            return;
        }
        String payDetails = "";
        String version = "3";

        String cardMoneyStr = "";
        if (cardMoney != 0) {
            cardMoneyStr = cardMoney + "";
        }
        log.info("加密前:{}", version + merId + (int) payMoney + cardMoneyStr+"" + orderId + payResult
            + privateField + payDetails + privateKey);
        
        String combineString="";
        if (cardMoney != 0) {
            combineString = version + "|" + merId + "|" + payMoney + "|" + cardMoney + "|" + orderId + "|" + payResult + "|" + privateField + "|" + payDetails + "|" + privateKey;
        } else {
            combineString = version + merId + payMoney + orderId + payResult + privateField + payDetails + privateKey;
        }
        String md5Str = MD5Util.genMD5Code(combineString);
        log.info("加密后md5:{}", md5Str);
        log.info("参数md5Str:{}", md5String);
        if (!StringUtils.isEmpty(md5Str) && md5String.equals(md5Str)) {
            if (payResult == 1) {
                paySuccess = true;
            }
        }
        if (!paySuccess) {
            rechargeOrder.setStatus(4);
            this.rechargeOrderMapper.updateByPrimaryKey(rechargeOrder);
            return;
        }
        rechargeOrder.setPayMoney(payMoney);
        rechargeOrder.setStatus(3);
        rechargeOrder.setAssetAmount(new BigDecimal(payMoney).multiply(new BigDecimal("0.975")).divide(new BigDecimal(669), 3, BigDecimal.ROUND_DOWN));

        String requestNo = SequenceUtil.getNextId();
        rechargeOrder.setRequestNo(requestNo);

        this.rechargeOrderMapper.updateByPrimaryKey(rechargeOrder);

        //开始为用户增加资产
        AssetOperationDto assetOperationDto = new AssetOperationDto();
        assetOperationDto.setUid(rechargeOrder.getUserId());
        assetOperationDto.setRequestNo(requestNo);
        assetOperationDto.setBusinessSubject(BusinessSubject.RECHARGE);
        assetOperationDto.setAssetCode("USC");
        assetOperationDto.setAmount(rechargeOrder.getAssetAmount());
        assetOperationDto.setLockAmount(BigDecimal.ZERO);
        assetOperationDto.setLoanAmount(BigDecimal.ZERO);
        assetOperationDto.setAccountClass(AccountClass.ASSET);
        assetOperationDto.setAccountSubject(AccountSubject.DEPOSIT_CARD);
        assetOperationDto.setIndex(0);

        String walletRequestNo = SequenceUtil.getNextId();
        //减去备用金账户资产
        AssetOperationDto walletAssetOperationDto = new AssetOperationDto();
        walletAssetOperationDto.setUid(uscwalletUserId);
        walletAssetOperationDto.setRequestNo(walletRequestNo);
        walletAssetOperationDto.setBusinessSubject(BusinessSubject.RECHARGE_DEDUCT);
        walletAssetOperationDto.setAssetCode("USC");
        walletAssetOperationDto.setAmount(rechargeOrder.getAssetAmount().negate());
        walletAssetOperationDto.setLockAmount(BigDecimal.ZERO);
        walletAssetOperationDto.setLoanAmount(BigDecimal.ZERO);
        walletAssetOperationDto.setAccountClass(AccountClass.ASSET);
        walletAssetOperationDto.setAccountSubject(AccountSubject.WITHDRAW_CARD);
        walletAssetOperationDto.setIndex(1);

        List<AssetOperationDto> assetOperationDtos = Lists.newArrayList(assetOperationDto);
        assetOperationDtos.add(walletAssetOperationDto);
        try {
            this.userAccountFacade.assetOperation(assetOperationDtos);
        } catch (Exception e) {
            log.info("给用户充值失败:{}", e.getMessage());
            throw e;
        }

    }

}
