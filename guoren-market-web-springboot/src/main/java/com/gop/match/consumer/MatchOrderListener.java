package com.gop.match.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.gop.code.consts.CommonCodeConst;
import com.gop.common.SendMessageService;
import com.gop.config.properties.RabbitmqConfig;
import com.gop.consumer.BaseListener;
import com.gop.domain.TradeOrder;
import com.gop.domain.enums.SendStatus;
import com.gop.exception.AppException;
import com.gop.exchange.hessian.TradeServcie;
import com.gop.exchange.model.Trade;
import com.gop.match.dto.MatchOrderDetail;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.TradeServiceManager;
import com.gop.mode.vo.ProduceLogVo;
import com.gop.notify.dto.NotifyDto;
import com.gop.notify.dto.enums.NotifyType;

import lombok.extern.slf4j.Slf4j;

@Component("matchOrderListener")
@Slf4j
public class MatchOrderListener extends BaseListener {

	@Autowired
	private MatchOrderService matchOrderService;

	@Autowired
	private SendMessageService sendMessageService;

	@Autowired
	private RabbitmqConfig rabbitmqConfig;

	@Autowired
	TradeServiceManager tradeServiceManager;

	@Override
	public void onMessage(JSONObject json) {
		String outerNo = json.getString("outNo");
		Integer uid = json.getInteger("uid");
		String symbol = json.getString("symbol");
		NotifyDto notifyDto = new NotifyDto();
		notifyDto.setUserId(uid);
		notifyDto.setNotifyType(NotifyType.MATCH_ORDER);
		Trade trade = null;
		try {
			trade = matchOrderService.payMatchOrderByOuterOrderNo(uid, outerNo);
			if (null == trade) {
				return;
			}
			TradeServcie tradeServcie = tradeServiceManager.getTradeService(symbol);
			tradeServcie.trade(trade);
			matchOrderService.updateSendStatusByInnerOrderNo(SendStatus.SEND, trade.getTransactionNo());

			notifyDto.setData(null);
		} catch (AppException e) {
			log.error("发现业务异常{}", e);
			return ;

		} catch (Exception e) {

			throw new AppException(CommonCodeConst.SERVICE_ERROR);

		}
		TradeOrder tradOrder = matchOrderService.queryTradeOrderInnerOrderNo(trade.getTransactionNo());
		notifyDto.setCode(CommonCodeConst.SERIVCE_SUCCESS);
		notifyDto.setData(new MatchOrderDetail(tradOrder));
		ProduceLogVo logVo = new ProduceLogVo();
		logVo.setExchangeName(rabbitmqConfig.getExchange());
		logVo.setKey(rabbitmqConfig.getCallBackBusinessKey());
		logVo.setMessage(notifyDto);
		long id = sendMessageService.tryMessage(logVo);
		sendMessageService.commitMessage(id);
	}

}
