package com.gte.consignationTrans.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinFlag;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class MatchOrderDetail {
	
	private Date createTime;

	private BigDecimal tradedNumber;

	private BigDecimal numberOver;
	// 交易号
	private String outerOrderNo;
	private String payTransactionNo;
	// 下单类型
	private TradeCoinType tradeCoinType;
	// 下单种类
	private TradeCoinFlag tradeCoinFlag;
	// 交易对
	private String symbol;
	// 下单价格
	private  BigDecimal price;
	// 下单数量
	private BigDecimal number;
	// 成交金额
	private BigDecimal matchedMoney;
	private BigDecimal moneyOver;
	private BigDecimal money;
	

	// 交易状态
	private TradeCoinStatus tradeCoinStatus;
	
	private Integer uid;

	public MatchOrderDetail(TradeOrder tradeOrder) {
	
		this.matchedMoney = tradeOrder.getTradedMoney();
		this.number = tradeOrder.getNumber();
		this.tradeCoinStatus = tradeOrder.getStatus();
		this.price = tradeOrder.getPrice();
		this.symbol = tradeOrder.getSymbol();
		this.tradeCoinType = tradeOrder.getOrderType();
		this.outerOrderNo = tradeOrder.getOuterOrderNo();
		this.tradeCoinFlag = tradeOrder.getTradeFlag();
		this.numberOver = tradeOrder.getNumberOver();
		this.tradedNumber = tradeOrder.getTradedNumber();
		this.moneyOver = tradeOrder.getMoneyOver();
		this.money = tradeOrder.getMoney();
		this.createTime = tradeOrder.getCreateDate();
		this.payTransactionNo=tradeOrder.getInnerOrderNo();
		this.uid=tradeOrder.getUid();
	}
}
