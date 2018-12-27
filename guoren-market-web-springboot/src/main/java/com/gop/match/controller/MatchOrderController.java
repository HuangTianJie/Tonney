package com.gop.match.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MatchCodeConst;
import com.gop.code.consts.OrderCodeConst;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.TradeMatchResult;
import com.gop.domain.TradeOrder;
import com.gop.domain.UserPaypasswordRelationship;
import com.gop.domain.enums.SendStatus;
import com.gop.domain.enums.SymbolStatus;
import com.gop.domain.enums.TradeCoinFlag;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import com.gop.exception.AppException;
import com.gop.exchange.hessian.TradeServcie;
import com.gop.exchange.model.Trade;
import com.gop.exchange.model.TradeResult;
import com.gop.exchange.model.modelenum.Type;
import com.gop.match.dto.MatchOrderDetail;
import com.gop.match.dto.MatchOrderDto;
import com.gop.match.dto.MatchRecordDto;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.SymbolService;
import com.gop.match.service.TradeRecordService;
import com.gop.match.service.TradeServiceManager;
import com.gop.mode.vo.PageModel;
import com.gop.quantfund.QuantFundConfigSymbolService;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserPaypasswordRelatationService;
import com.gop.util.BigDecimalUtils;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/match")
@Slf4j
public class MatchOrderController {

	@Autowired
	SymbolService symbolService;
	@Autowired
	TradeServiceManager tradeServiceManager;

	@Autowired
	MatchOrderService matchOrderService;

	@Autowired
	TradeRecordService tradeRecordService;
	
	@Autowired
	QuantFundConfigSymbolService quantFundConfigSymbolService;
	
	@Autowired
	private UserFacade userFacade;
	
	@Autowired
	private UserPaypasswordRelatationService userPaypasswordRelatationService;

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public void matchOrder(@AuthForHeader AuthContext authContext, @RequestParam("orderNo") String orderNo,
			@RequestParam("tradeCoinType") TradeCoinType tradeCoinType,
			@RequestParam("tradeCoinFlag") TradeCoinFlag tradeCoinFlag, @RequestParam("symbol") String symbol,
			@RequestParam(required = false, value = "price") BigDecimal price,
			@RequestParam(required = false, value = "amount") BigDecimal amount,
			@RequestParam(required = false, value = "market") BigDecimal market) {
		
		Integer uid = authContext.getLoginSession().getUserId();
		String payPassword = authContext.getpayPassword();
		//查询用户是否需要输入验证码 0一直输入  1 从不输入 2两小时输入一次
		UserPaypasswordRelationship info = userPaypasswordRelatationService.selectByUid(uid);
		if(info.getInputType().equals(0)) {
			userFacade.checkPayPassword(uid, payPassword);
			//更新资金密码更新时间
			UserPaypasswordRelationship param = new UserPaypasswordRelationship();
			param.setUid(uid);
			param.setUpdateDate(new Date());
			userPaypasswordRelatationService.updateByUid(param);
		}else if(info.getInputType().equals(2) && info.getUpdateDate().before(new Date(new Date().getTime()-7200000))){
			userFacade.checkPayPassword(uid, payPassword);
			//更新资金密码更新时间
			UserPaypasswordRelationship param = new UserPaypasswordRelationship();
			param.setUid(uid);
			param.setUpdateDate(new Date());
			userPaypasswordRelatationService.updateByUid(param);
		}
		
		ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(symbol);
		if (null == configSymbol) {
			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
		}

		if (configSymbol.getStatus().equals(SymbolStatus.INIT)) {
			throw new AppException(MatchCodeConst.SYMBOL_NOT_OPEN);
		}

		if (configSymbol.getStatus().equals(SymbolStatus.DELISTED)) {
			throw new AppException(MatchCodeConst.SYMBOL_CLOSED);
		}

		TradeServcie tradeServcie = tradeServiceManager.getTradeService(symbol);
		if (tradeServcie == null) {
			log.error("未发现symbol:{} url", symbol);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		// 校验
		MatchOrderDto matchOrderDto = new MatchOrderDto();
		matchOrderDto.setUserId(authContext.getLoginSession().getUserId());
		matchOrderDto.setAmount(amount);
		matchOrderDto.setMarket(market);
		matchOrderDto.setOutOrderNo(orderNo);
		matchOrderDto.setPrice(price);
		matchOrderDto.setSymbol(symbol);
		matchOrderDto.setTradeCoinFlag(tradeCoinFlag);
		matchOrderDto.setTradeCoinType(tradeCoinType);
		CheckOrderParamer(configSymbol, matchOrderDto);
		//交易对为基金交易对时进行校验
		quantFundConfigSymbolService.checkQuantFundConfigSymbolOrder(symbol, matchOrderDto);
		
		Trade trade = matchOrderService.createAndPayMatchOrder(10003, matchOrderDto);   //marked by hoff 20181002向trade_order表插入订单数据
		try {
			tradeServcie.trade(trade); /*通过hession(使用HessianClientInterceptor创建marketWeb与tradeEngine服务的调用关系)远程调用tradeEngine的TradeService服务 marked by hoff  20181002
			                            主要更新trade_order_buy_{symbol}或者trade_order_seller_{symbol}数据表
			                             */
			matchOrderService.updateSendStatusByInnerOrderNo(SendStatus.SEND, trade.getTransactionNo());
		} catch (Exception e) {
			log.error("下单出现异常：{}", e);
		}

	}

	private void CheckOrderParamer(ConfigSymbol configSymbol, MatchOrderDto matchOrderDto) {

		if (matchOrderDto.getTradeCoinFlag().equals(TradeCoinFlag.FIXED)) {
			BigDecimal num = matchOrderDto.getAmount();
			BigDecimal price = matchOrderDto.getPrice();

			if (null == num || price == null || !BigDecimalUtils.isBigger(num, BigDecimal.ZERO)
					|| !BigDecimalUtils.isBigger(price, BigDecimal.ZERO)) {
				throw new AppException(CommonCodeConst.FIELD_ERROR);
			}

			if (BigDecimalUtils.isBigger(price, configSymbol.getMaxAmount1())) {
				throw new AppException(OrderCodeConst.MAX_ORDER_PRICE_ERROR, "交易价格过高",
						configSymbol.getMaxAmount1().setScale(configSymbol.getMinPrecision1()).toString(),
						configSymbol.getAssetCode1());
			}
			if (BigDecimalUtils.isBigger(configSymbol.getMinAmount1(), price)) {
				throw new AppException(OrderCodeConst.MIN_ORDER_PRICE_ERROR, "交易价格过低",
						configSymbol.getMinAmount1().setScale(configSymbol.getMinPrecision1()).toString(),
						configSymbol.getAssetCode1());
			}
			if (BigDecimalUtils.isBigger(num, configSymbol.getMaxAmount2())) {
				throw new AppException(OrderCodeConst.MAX_ORDER_COIN_ERROR, "交易数量过高",
						configSymbol.getMaxAmount2().setScale(configSymbol.getMinPrecision2()).toString(),
						configSymbol.getAssetCode2());
			}
			if (BigDecimalUtils.isBigger(configSymbol.getMinAmount2(), num)) {
				throw new AppException(OrderCodeConst.MIN_ORDER_COIN_ERROR, "交易数量过低",
						configSymbol.getMinAmount2().setScale(configSymbol.getMinPrecision2()).toString(),
						configSymbol.getAssetCode2());
			}
			if (configSymbol.getMinPrecision1() < price.scale()) {
				throw new AppException(OrderCodeConst.PFRCISION_ERROR, "交易精度错误",
						configSymbol.getMinPrecision1().toString());
			}
			if (configSymbol.getMinPrecision2() < num.scale()) {
				throw new AppException(OrderCodeConst.PFRCISION_ERROR, "交易精度错误",
						configSymbol.getMinPrecision2().toString());
			}

		} else {
			if (matchOrderDto.getTradeCoinType().equals(TradeCoinType.BUY)) {
				BigDecimal money = matchOrderDto.getMarket();
				if (null == money || !BigDecimalUtils.isBigger(money, BigDecimal.ZERO)) {
					throw new AppException(CommonCodeConst.FIELD_ERROR);
				}
				if (BigDecimalUtils.isBigger(money,
						configSymbol.getMaxAmount1().multiply(configSymbol.getMaxAmount2()))) {
					throw new AppException(OrderCodeConst.MAX_ORDER_AMOUNT_ERROR, "交易金额过高",
							configSymbol.getMaxAmount1().multiply(configSymbol.getMaxAmount2())
									.setScale(configSymbol.getMinPrecision1()).toString(),
							configSymbol.getAssetCode1());
				}
				if (BigDecimalUtils.isBigger(configSymbol.getMinAmount1().multiply(configSymbol.getMinAmount2()),
						money)) {
					throw new AppException(OrderCodeConst.MIN_ORDER_AMOUNT_ERROR, "交易金额过低",
							configSymbol.getMinAmount1().multiply(configSymbol.getMinAmount2())
									.setScale(configSymbol.getMinPrecision1()).toString(),
							configSymbol.getAssetCode1());
				}
				if (configSymbol.getMinPrecision1() < money.scale()) {
					throw new AppException(OrderCodeConst.PFRCISION_ERROR, "金额精度错误",
							configSymbol.getMinPrecision1().toString());
				}

			} else {
				BigDecimal num = matchOrderDto.getAmount();
				if (null == num || !BigDecimalUtils.isBigger(num, BigDecimal.ZERO)) {
					throw new AppException(CommonCodeConst.FIELD_ERROR);
				}
				if (BigDecimalUtils.isBigger(num, configSymbol.getMaxAmount2())) {
					throw new AppException(OrderCodeConst.MAX_ORDER_COIN_ERROR, "交易数量过高",
							configSymbol.getMaxAmount2().setScale(configSymbol.getMinPrecision2()).toString(),
							configSymbol.getAssetCode2());
				}
				if (BigDecimalUtils.isBigger(configSymbol.getMinAmount2(), num)) {
					throw new AppException(OrderCodeConst.MIN_ORDER_COIN_ERROR, "交易数量过低",
							configSymbol.getMinAmount2().setScale(configSymbol.getMinPrecision2()).toString(),
							configSymbol.getAssetCode2());
				}

				if (configSymbol.getMinPrecision2() < num.scale()) {
					throw new AppException(OrderCodeConst.PFRCISION_ERROR, "挂单数量精度错误",
							configSymbol.getMinPrecision2().toString());
				}
			}

		}

	}

	// 查询历史下单记录
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/match-order/history", method = RequestMethod.GET)
	public PageModel<MatchOrderDetail> getHistoryMatchOrder(@AuthForHeader AuthContext authContext,
			@RequestParam("pageNo") Integer pageNo, @RequestParam(value = "symbol", required = false) String symbol,
			@RequestParam(value = "orderType", required = false) String orderType,
			@RequestParam("pageSize") Integer pageSize) {
		PageModel<MatchOrderDetail> pageModelDto = null;
		TradeCoinType type = null;
		if("BUY".equals(orderType)) {
			type = TradeCoinType.BUY;
		}else if("SELL".equals(orderType)) {
			type = TradeCoinType.SELL;
		}
		PageModel<TradeOrder> pageModel = matchOrderService.queryHistoryTradeOrderByPage(symbol,
				authContext.getLoginSession().getUserId(), type, pageNo, pageSize);
		pageModelDto = new PageModel<MatchOrderDetail>();
		if (null != pageModel) {

			List<MatchOrderDetail> lists = pageModel.getList().stream().map(a -> new MatchOrderDetail(a))
					.collect(Collectors.toList());
			pageModelDto.setPageNo(pageModel.getPageNo());
			pageModelDto.setPageNum(pageModel.getPageNum());
			pageModelDto.setPageSize(pageModel.getPageSize());
			pageModelDto.setList(lists);
		}

		return pageModelDto;

	}

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/match-order/match-record", method = RequestMethod.GET)
	public PageModel<MatchRecordDto> getMatchRecord(@AuthForHeader AuthContext authContext,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("symbol") String symbol,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("orderNo") String orderNo) {
		PageModel<MatchRecordDto> pageModelDto = null;
		TradeOrder tradeOrder = matchOrderService.queryTradeOrderInnerOrderNo(orderNo);
		if (null == tradeOrder) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		PageModel<TradeMatchResult> pageModel = tradeRecordService.getTradeMatchResult(orderNo,
				tradeOrder.getOrderType(), pageNo, pageSize);
		pageModelDto = new PageModel<MatchRecordDto>();
		if (null != pageModel) {

			List<MatchRecordDto> lists = pageModel.getList().stream().map(a -> new MatchRecordDto(a))
					.collect(Collectors.toList());
			pageModelDto.setPageNo(pageModel.getPageNo());
			pageModelDto.setPageNum(pageModel.getPageNum());
			pageModelDto.setPageSize(pageModel.getPageSize());
			pageModelDto.setList(lists);
		}

		return pageModelDto;

	}

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/match-order/current", method = RequestMethod.GET)
	public PageModel<MatchOrderDetail> getMatchOrderDetail(@AuthForHeader AuthContext authContext,
			@RequestParam("pageNo") Integer pageNo, @RequestParam(value = "symbol", required = false) String symbol,
			@RequestParam(value = "orderType", required = false) String orderType,@RequestParam("pageSize") Integer pageSize) {
		PageModel<MatchOrderDetail> pageModelDto = null;
		TradeCoinType type = null;
		if("BUY".equals(orderType)) {
			type = TradeCoinType.BUY;
		}else if("SELL".equals(orderType)) {
			type = TradeCoinType.SELL;
		}
		PageModel<TradeOrder> pageModel = matchOrderService
				.queryCurrentTradeOrderByPage(authContext.getLoginSession().getUserId(), symbol,type, pageNo, pageSize);
		pageModelDto = new PageModel<MatchOrderDetail>();
		if (null != pageModel) {

			List<MatchOrderDetail> lists = pageModel.getList().stream().map(a -> new MatchOrderDetail(a))
					.collect(Collectors.toList());
			pageModelDto.setPageNo(pageModel.getPageNo());
			pageModelDto.setPageNum(pageModel.getPageNum());
			pageModelDto.setPageSize(pageModel.getPageSize());
			pageModelDto.setList(lists);
		}

		return pageModelDto;
	}

	// 撤单
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/match-order/cancel", method = RequestMethod.GET)
	public void cancelMatchOrder(@AuthForHeader AuthContext authContext, @RequestParam("orderNo") String macthOrderNo,
			@RequestParam("symbol") String symbol) {

		ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(symbol);
//		if (null == configSymbol || !configSymbol.getStatus().equals(SymbolStatus.LISTED)) {
//			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
//		}
		if (null == configSymbol || configSymbol.getStatus().equals(SymbolStatus.INIT)) {
			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
		}
		Integer uid = authContext.getLoginSession().getUserId();
		TradeServcie tradeServcie = tradeServiceManager.getTradeService(symbol);
		if (tradeServcie == null) {
			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
		}
		TradeResult tradeResult;
		TradeOrder tradeOrder = matchOrderService.queryTradeOrderInnerOrderNo(macthOrderNo);
		if (tradeOrder == null || !tradeOrder.getUid().equals(uid) || !tradeOrder.getSymbol().equals(symbol)) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		if (tradeOrder.getStatus().equals(TradeCoinStatus.CANCEL)) {
			throw new AppException(OrderCodeConst.ORDER_HAD_CANCELLED);
		}

		try {
			tradeResult = tradeServcie.cancel(macthOrderNo,
					tradeOrder.getOrderType().equals(TradeCoinType.BUY) ? Type.BUY : Type.SELL);
		} catch (Exception e) {
			log.info("发现异常", e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			log.error("statck", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		matchOrderService.cancelOrder(uid, macthOrderNo, symbol, tradeResult.getPrice(), tradeResult.getMarketOver(),
				tradeResult.getOverNum());
	}
	
}
