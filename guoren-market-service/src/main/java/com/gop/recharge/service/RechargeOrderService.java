package com.gop.recharge.service;

public interface RechargeOrderService {

    public String doRecharge(int uscwalletUserId, int userId, Long payMoney, String asset, String payChannel,
            int payType, String merId, String privateKey, String desKey, String postUrl, String cardNum,
            String cardInfo, String returnUrl);

    public void doCallback(int uscwalletUserId, String merId, long payMoney, long cardMoney, String orderId,
            int payResult, String privateField, String privateKey, String md5String);

}
