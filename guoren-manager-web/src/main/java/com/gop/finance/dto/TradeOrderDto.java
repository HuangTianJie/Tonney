package com.gop.finance.dto;

import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinFlag;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by wuyanjie on 2018/4/26.
 */
@Data
public class TradeOrderDto {
    //@ApiModelProperty("挂单时间")
    Date createDate;
    //@ApiModelProperty("成交时间")
    Date finishDate;
    //@ApiModelProperty("交易对")
    String symbol;
    //@ApiModelProperty("下单类型")
    TradeCoinType tradeType;
    //@ApiModelProperty("下单种类")
    TradeCoinFlag tradeFlag;
    //@ApiModelProperty("下单流水号")
    String requestNo;
    //@ApiModelProperty("用户id")
    Integer uid;
    //@ApiModelProperty("下单数量")
    BigDecimal number;
    //@ApiModelProperty("下单价格")
    BigDecimal price;
    //@ApiModelProperty("已撮合数量")
    BigDecimal tradedNumber;
    //@ApiModelProperty("剩余数量")
    BigDecimal numberOver;
    //@ApiModelProperty("已撮合金额")
    BigDecimal tradedMoney;
    //@ApiModelProperty("交易手续费")
    BigDecimal fee;
    //@ApiModelProperty("订单状态")
    TradeCoinStatus status;

    public TradeOrderDto(TradeOrder tradeOrder){
        this.createDate = tradeOrder.getCreateDate();
        if(tradeOrder.getStatus().equals(TradeCoinStatus.SUCCESS)) {
            this.finishDate = tradeOrder.getUpdateDate();
        }
        this.symbol = tradeOrder.getSymbol();
        this.tradeType = tradeOrder.getOrderType();
        this.tradeFlag = tradeOrder.getTradeFlag();
        this.requestNo = tradeOrder.getRequestNo();
        this.uid = tradeOrder.getUid();
        this.number = tradeOrder.getNumber();
        this.price = tradeOrder.getPrice();
        this.tradedNumber = tradeOrder.getTradedNumber();
        this.numberOver = tradeOrder.getNumberOver();
        this.tradedMoney = tradeOrder.getTradedMoney();
        this.fee = tradeOrder.getFee();
        this.status = tradeOrder.getStatus();
    }

}
