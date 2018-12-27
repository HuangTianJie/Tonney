package com.gop.match.consumer;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.gop.consumer.BaseListener;
import com.gop.match.facade.MatchTradeFacade;
import com.gop.util.CheckStateUtil;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

@Component("matchRecordListener")
@Slf4j
public class MatchRecordListener extends BaseListener {

	@Autowired
	MatchTradeFacade matchTradeFacade;

	@Override
	public void onMessage(JSONObject message) {

		JSONObject jsonObject = null;
		String buyOrderNo = null;
		String sellOrderNo = null;
		Integer buyId = null;
		Integer sellId = null;
		BigDecimal num = null;
		BigDecimal price = null;
		String symbol = null;

		// 生成撮合成功买入订单号
		String buySuccessOrder = SequenceUtil.getNextId();
		// 生成撮合成功卖出订单号
		String sellSuccessOrder = SequenceUtil.getNextId();
		try {
			jsonObject = message;
			buyOrderNo = jsonObject.getString("buyTxno");
			CheckStateUtil.checkArgumentState(null == buyOrderNo, "buyOrderNo is null");
			sellOrderNo = jsonObject.getString("sellTxno");
			CheckStateUtil.checkArgumentState(null == sellOrderNo, "sellOrderNo is null");
			buyId = jsonObject.getInteger("buyId");
			CheckStateUtil.checkArgumentState(null == buyId, "buyId is null");
			sellId = jsonObject.getInteger("sellId");
			CheckStateUtil.checkArgumentState(null == sellId, "sellId is null");
			num = jsonObject.getBigDecimal("num");
			CheckStateUtil.checkArgumentState(null == num, "num is null");
			price = jsonObject.getBigDecimal("price");
			CheckStateUtil.checkArgumentState(null == price, "price is null");
			symbol = jsonObject.getString("symbol");
			CheckStateUtil.checkArgumentState(null == symbol, "symbol is null");
		} catch (Exception e) {
			log.error("消息" + message + "解析异常:" + e);
			return;
		}
		matchTradeFacade.matchRecord(buyOrderNo, sellOrderNo, buyId, sellId, num, price, symbol);
	}

}
