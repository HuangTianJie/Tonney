package com.gop.domain;

import com.gop.domain.enums.SendStatus;
import com.gop.domain.enums.TradeCoinFlag;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户订单表：包括买单和卖单
 */
@Data
@ToString
public class TradeOrder {
	private Integer id;

	/**
	 * 用户id
	 */
	private Integer uid;

	/**
	 * 券商id
	 */
	private Integer brokerId;

	/**
	 * 帐户id：冻结那个帐户的资产
	 */
	private Integer accountId;

	/**
	 * 交易代码
	 */
	private String symbol;

	/**
	 * 外部订单号
	 */
	private String outerOrderNo;

	/**
	 * 内部订单号
	 */
	private String innerOrderNo;

	/**
	 * 资产变化请求号，相当于交易流水号
	 */
	private String requestNo;

	/**
	 * 下单数字币数量
	 */
	private BigDecimal number;

	/**
	 * 下单价格
	 */
	private BigDecimal price;

	/**
	 * 下单现金金额
	 */
	private BigDecimal money;

	/**
	 * 剩余币数
	 */
	private BigDecimal numberOver;

	/**
	 * 剩余钱数
	 */
	private BigDecimal moneyOver;

	/**
	 * 下单类型 'BUY','SELL'
	 */
	private TradeCoinType orderType;

	/**
	 * 下单种类 'FIXED','MARKET'
	 */
	private TradeCoinFlag tradeFlag;

	/**
	 * 已撮合数量
	 */
	private BigDecimal tradedNumber;

	/**
	 * 已撮合金额
	 */
	private BigDecimal tradedMoney;

	/**
	 * 订单状态  'FAIL','WAITING','CANCEL','SUCCESS','PROCESSING'
	 */
	private TradeCoinStatus status;

	/**
	 * 订单撮合状态 'SEND','WAIT','UNKNOWN'
	 */
	private SendStatus sendStatus;

	private Date createDate;

	private Date updateDate;

	private String failMessageCode;

	private String failMessageDes;

	/**
	 * 总收取手续费
	 */
	private BigDecimal fee;

}