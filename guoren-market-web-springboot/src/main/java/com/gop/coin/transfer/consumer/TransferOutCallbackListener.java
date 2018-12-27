package com.gop.coin.transfer.consumer;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.gop.code.consts.CommonCodeConst;
import com.gop.coin.transfer.dto.mq.TransferOutCallbackDto;
import com.gop.coin.transfer.factory.WithdrawCoinServiceFactory;
import com.gop.coin.transfer.service.WithdrawCoinService;
import com.gop.config.properties.RabbitmqConfig;
import com.gop.consumer.BaseListener;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Component("transferOutCallbackListener")
@Slf4j
public class TransferOutCallbackListener extends BaseListener {

	// @Autowired
	// private

	@Autowired
	private WithdrawCoinService withdrawCoinService;

	@Autowired
	private RabbitmqConfig config;

	@Override
	public void onMessage(JSONObject json) {

		log.info("receive message : {}", json.toJSONString());
		TransferOutCallbackDto dto = JSONObject.toJavaObject(json, TransferOutCallbackDto.class);

		String assetCode = dto.getAssetCode();
		String txid = dto.getTxId();
		String outerOrder = dto.getOrderId();
		DepositCoinAssetStatus status = dto.getAssetStatus();
		BigDecimal chainFee = dto.getTxFee();
		if (null == status || Strings.isNullOrEmpty(txid) || Strings.isNullOrEmpty(assetCode)) {
			log.info("订单字段异常状态异常，状态字段无法解析");
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单字段异常状态异常，状态字段无法解析");
		}

		switch (status) {
		case CONFIRM:
			// 确认状态不需要处理
			return;
		case SUCCESS:
			withdrawCoinService.withdrawConfirm(txid,chainFee);
			break;
		case FAILURE:
			withdrawCoinService.withdrawFail(txid, dto.getSendMessage());
			break;
		default:
			log.info("无法识别的订单状态.资产{}订单号:{},状态:", assetCode, outerOrder, status);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单状态状态异常");
		}

	}

}
