package com.gop.coin.transfer.consumer;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.gop.code.consts.CommonCodeConst;
import com.gop.coin.transfer.dto.mq.TransferInDto;
import com.gop.coin.transfer.service.DepositCoinOrderService;
import com.gop.consumer.BaseListener;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Component("transferInListener")
@Slf4j
public class TransferInListener extends BaseListener {

	@Autowired
	private DepositCoinOrderService depositCoinOrderService;

	@Override
	public void onMessage(JSONObject json) {

		log.info("receive message : {}", json.toJSONString());
		TransferInDto dto = JSONObject.toJavaObject(json, TransferInDto.class);

		String assetCode = dto.getAssetCode();
		String fromAddress = dto.getFromWallet();
		String toAddress = dto.getToWallet();
		BigDecimal amount = dto.getAmount();
		String outerOrder = dto.getOrderId();
		String sendMessage = dto.getSendMessage();
		DepositCoinAssetStatus status = dto.getAssetStatus();
		if (null == status || Strings.isNullOrEmpty(outerOrder) || Strings.isNullOrEmpty(assetCode)
				|| Strings.isNullOrEmpty(toAddress) || null == amount) {
			log.info("订单字段异常状态异常，状态字段无法解析");
			throw new AppException(CommonCodeConst.SERVICE_ERROR,"订单字段异常状态异常，状态字段无法解析");
		}

		if (null == fromAddress) {
			fromAddress = "";
		}

		switch (status) {
		case CONFIRM:
			depositCoinOrderService.coinDepositOrder(assetCode, toAddress, amount, outerOrder, sendMessage);
			break;
		case SUCCESS:
			depositCoinOrderService.depositConfirm(assetCode, toAddress, amount, outerOrder, sendMessage);
			break;
		case FAILURE:
			depositCoinOrderService.depositCancel(assetCode, toAddress, amount, outerOrder, sendMessage);
			break;
		default:
			log.info("无法识别的订单状态.资产{}订单号:{},状态:", assetCode, outerOrder, status);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "未知订单状态");
		}
	}

}
