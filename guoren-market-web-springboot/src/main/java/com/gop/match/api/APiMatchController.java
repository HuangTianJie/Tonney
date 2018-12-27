package com.gop.match.api;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MatchCodeConst;
import com.gop.code.consts.OrderCodeConst;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.TradeOrder;
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
import com.gop.match.dto.BatchMatchOrderDto;
import com.gop.match.dto.BatchMatchOrderResponse;
import com.gop.match.dto.BatchMatchOrderResponse.Response;
import com.gop.match.dto.CancelMatchOrderDto;
import com.gop.match.dto.MatchOrderDetail;
import com.gop.match.dto.MatchOrderDto;
import com.gop.match.dto.QueryOrderDto;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.SymbolService;
import com.gop.match.service.TradeServiceManager;
import com.gop.mode.vo.PageModel;
import com.gop.util.ApiRequestTransaltor;
import com.gop.util.BigDecimalUtils;
import com.gop.util.dto.ApiRequestExtractDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class APiMatchController {

	@Autowired
	private MatchOrderService matchOrderService;
	@Autowired
	private TradeServiceManager tradeServiceManager;

	@Autowired
	private ApiRequestTransaltor apiRequestTransaltor;

	@Autowired
	SymbolService symbolService;

	@RequestMapping("/matchOrder")
	public void MatchApi(@RequestBody String jsonString) {
		ApiRequestExtractDto<MatchOrderDto> apiRequestExtractDto = apiRequestTransaltor.handlerMatchBack(jsonString,
				MatchOrderDto.class);
		MatchOrderDto matchDto = apiRequestExtractDto.getData();
		ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(matchDto.getSymbol());
		if (null == configSymbol || !configSymbol.getStatus().equals(SymbolStatus.LISTED)) {
			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
		}
		TradeServcie tradeServcie = tradeServiceManager.getTradeService(configSymbol.getSymbol());
		if (tradeServcie == null) {
			log.error("未发现symbol:{} url", configSymbol.getSymbol());
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		CheckOrderParamer(configSymbol, matchDto);
		matchDto.setUserId(apiRequestExtractDto.getUid());
		Trade trade = matchOrderService.createAndPayMatchOrder(apiRequestExtractDto.getBrokerId(), matchDto);
		try {
			tradeServcie.trade(trade);
			matchOrderService.updateSendStatusByInnerOrderNo(SendStatus.SEND, trade.getTransactionNo());
		} catch (Exception e) {
			log.error("下单出现异常：{}", e);
		}
	}

	@RequestMapping("/batchMatchOrder")
	public BatchMatchOrderResponse batchMatchApi(@RequestBody String jsonString) {
		ApiRequestExtractDto<BatchMatchOrderDto> apiRequestExtractDto = apiRequestTransaltor.handlerMatchBack(jsonString, BatchMatchOrderDto.class);
		BatchMatchOrderDto batchMatchOrderDto = apiRequestExtractDto.getData();

		BatchMatchOrderResponse result = new BatchMatchOrderResponse();
		List<Response> responses = result.getResponses();
		for (MatchOrderDto matchDto : batchMatchOrderDto.getMatchOrderDtos()) {
			try {
				ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(matchDto.getSymbol());
				if (null == configSymbol || !configSymbol.getStatus().equals(SymbolStatus.LISTED)) {
					throw new AppException(MatchCodeConst.INVALID_SYMBOL);
				}
				TradeServcie tradeServcie = tradeServiceManager.getTradeService(configSymbol.getSymbol());
				if (tradeServcie == null) {
					log.error("未发现symbol:{} url", configSymbol.getSymbol());
					throw new AppException(CommonCodeConst.SERVICE_ERROR);
				}
				CheckOrderParamer(configSymbol, matchDto);
				matchDto.setUserId(apiRequestExtractDto.getUid());
				Trade trade = matchOrderService.createAndPayMatchOrder(apiRequestExtractDto.getBrokerId(), matchDto);
				tradeServcie.trade(trade);
				matchOrderService.updateSendStatusByInnerOrderNo(SendStatus.SEND, trade.getTransactionNo());
				responses.add(Response.builder().orderNo(matchDto.getOutOrderNo()).returnCode(CommonCodeConst.SERIVCE_SUCCESS).returnMsg(CommonCodeConst.SERIVCE_SUCCESS).build());
			} catch (AppException e) {
				log.error("批量下单异常", e);
				responses.add(Response.builder().orderNo(matchDto.getOutOrderNo()).returnCode(e.getErrCode()).returnMsg(e.getErrCode()).build());
			} catch (Exception e) {
				log.error("下单失败", e);
				responses.add(Response.builder().orderNo(matchDto.getOutOrderNo()).returnCode(CommonCodeConst.SERVICE_ERROR).returnMsg(CommonCodeConst.SERVICE_ERROR).build());
			}
		}
		return result;
	}

	@RequestMapping("/orderquery")
	public MatchOrderDetail orderquery(@RequestBody String jsonString) {

		ApiRequestExtractDto<QueryOrderDto> apiRequestExtractDto = apiRequestTransaltor.handlerMatchBack(jsonString,
				QueryOrderDto.class);
		QueryOrderDto queryOrderDto = apiRequestExtractDto.getData();

		if (null == queryOrderDto | Strings.isNullOrEmpty(queryOrderDto.getOutTradeNo())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		TradeOrder tradeOrder = matchOrderService.queryTradeOrderbyOuterOrderNo(apiRequestExtractDto.getUid(),
				queryOrderDto.getOutTradeNo());
		if (null == tradeOrder) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		if (!tradeOrder.getUid().equals(apiRequestExtractDto.getUid())) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		return new MatchOrderDetail(tradeOrder);
	}

	@RequestMapping("/matchOrder/process")
	public List<MatchOrderDetail> queryOrderProcess(@RequestBody String jsonString) {

		ApiRequestExtractDto<QueryOrderDto> apiRequestExtractDto = apiRequestTransaltor.handlerMatchBack(jsonString,
				QueryOrderDto.class);
		QueryOrderDto queryOrderDto = apiRequestExtractDto.getData();

		if (null == queryOrderDto || Strings.isNullOrEmpty(queryOrderDto.getSymbol())) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		PageModel<TradeOrder> pageModels = matchOrderService.queryCurrentTradeOrderByPage(apiRequestExtractDto.getUid(),
				queryOrderDto.getSymbol(),null, 1, 200);

		if (null == pageModels) {
			return new ArrayList<>();
		}
		List<TradeOrder> list = pageModels.getList();
		if (null == list) {
			return new ArrayList<>();
		}
		return list.stream().map(a -> new MatchOrderDetail(a)).collect(Collectors.toList());
	}

	@RequestMapping("/cancel/all")
	public List<String> cancelAllOrderApi(@RequestBody String jsonString) {

		ApiRequestExtractDto<QueryOrderDto> apiRequestExtractDto = apiRequestTransaltor.handlerMatchBack(jsonString,
				QueryOrderDto.class);
		QueryOrderDto queryOrderDto = apiRequestExtractDto.getData();

		List<String> cancelOrders = new ArrayList<>();
		List<TradeOrder> lists = null;
		if (Strings.isNullOrEmpty(queryOrderDto.getOutTradeNos())) {
			do {
				PageModel<TradeOrder> pageModel = matchOrderService
						.queryCurrentTradeOrderByPage(apiRequestExtractDto.getUid(), queryOrderDto.getSymbol(), null,1, 200);
				if (null == pageModel) {
					return cancelOrders;
				}
				lists = pageModel.getList();
				if (null == lists) {
					return cancelOrders;
				}

				TradeResult tradeResult;
				for (TradeOrder tradeOrder : lists) {
					if (!tradeOrder.getSendStatus().equals(SendStatus.SEND)) {
						continue;
					}
					tradeResult = null;
					try {
						TradeServcie tradeServcie = tradeServiceManager.getTradeService(tradeOrder.getSymbol());
						if (tradeServcie == null) {
							throw new AppException(MatchCodeConst.INVALID_SYMBOL, "无效的资产代码：" + tradeOrder.getSymbol());
						}
						tradeResult = tradeServcie.cancel(tradeOrder.getInnerOrderNo(),
								tradeOrder.getOrderType().equals(TradeCoinType.BUY) ? Type.BUY : Type.SELL);
					} catch (Exception e) {
						log.error("撤单发生异常symbol:{},trxno:{},error:{}", tradeOrder.getSymbol(),
								tradeOrder.getInnerOrderNo(), e.getMessage());

						return cancelOrders;
					}
					matchOrderService.cancelOrder(apiRequestExtractDto.getUid(), tradeOrder.getInnerOrderNo(),
							tradeOrder.getSymbol(), tradeResult.getPrice(), tradeResult.getMarketOver(),
							tradeResult.getOverNum());

					cancelOrders.add(tradeOrder.getOuterOrderNo());
				}
			} while (lists != null && lists.size() > 0);

		} else {
			CancelMatchOrderDto cancelMatchOrderDto = new CancelMatchOrderDto();
			List<String> outOrders = Splitter.on(",").splitToList(queryOrderDto.getOutTradeNos());
			for (String outOrder : outOrders) {
				cancelMatchOrderDto.setOutTradeNo(outOrder);
				TradeResult tradeResult = null;
				TradeOrder tradeOrder = null;
				try {
					tradeOrder = matchOrderService.queryTradeOrderbyOuterOrderNo(apiRequestExtractDto.getUid(),
							cancelMatchOrderDto.getOutTradeNo());
					if (tradeOrder == null) {
						throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
					}
					if (tradeOrder.getStatus().equals(TradeCoinStatus.CANCEL)) {
						throw new AppException(OrderCodeConst.ORDER_HAD_CANCELLED);
					}
					if (tradeOrder.getStatus().equals(TradeCoinStatus.WAITING)) {
						throw new AppException(OrderCodeConst.ORDER_HAD_PROCESSING);
					}
					if (tradeOrder.getStatus().equals(TradeCoinStatus.SUCCESS)) {
						throw new AppException(OrderCodeConst.ORDER_HAD_TRADED);
					}
					if (tradeOrder.getStatus().equals(TradeCoinStatus.FAIL)) {
						throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
					}
					TradeServcie tradeServcie = tradeServiceManager.getTradeService(tradeOrder.getSymbol());
					if (tradeServcie == null) {
						throw new AppException(MatchCodeConst.INVALID_SYMBOL);
					}
					try {
						tradeResult = tradeServcie.cancel(tradeOrder.getInnerOrderNo(),
								tradeOrder.getOrderType().equals(TradeCoinType.BUY) ? Type.BUY : Type.SELL);
					} catch (Exception e) {
						throw new AppException(CommonCodeConst.SERVICE_ERROR);
					}
					matchOrderService.cancelOrder(apiRequestExtractDto.getUid(), tradeOrder.getInnerOrderNo(),
							tradeOrder.getSymbol(), tradeResult.getPrice(), tradeResult.getMarketOver(),
							tradeResult.getOverNum());
				} catch (Exception e) {
					log.error("撤单发生异常symbol:{},trxno:{},error:{}", tradeOrder.getSymbol(), tradeOrder.getInnerOrderNo(),
							e.getMessage());
					return cancelOrders;
				}
				cancelOrders.add(outOrder);
			}
		}
		return cancelOrders;
	}

	@RequestMapping("/cancel")
	public void cancelOrderApi(@RequestBody String jsonString) {

		ApiRequestExtractDto<CancelMatchOrderDto> apiRequestExtractDto = apiRequestTransaltor
				.handlerMatchBack(jsonString, CancelMatchOrderDto.class);
		CancelMatchOrderDto cancelMatchOrderDto = apiRequestExtractDto.getData();

		TradeResult tradeResult = null;
		TradeOrder tradeOrder = matchOrderService.queryTradeOrderbyOuterOrderNo(apiRequestExtractDto.getUid(),
				cancelMatchOrderDto.getOutTradeNo());
		if (tradeOrder == null) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		if (tradeOrder.getStatus().equals(TradeCoinStatus.CANCEL)) {
			throw new AppException(OrderCodeConst.ORDER_HAD_CANCELLED);
		}
		if (tradeOrder.getStatus().equals(TradeCoinStatus.WAITING)) {
			throw new AppException(OrderCodeConst.ORDER_HAD_PROCESSING);
		}
		if (tradeOrder.getStatus().equals(TradeCoinStatus.SUCCESS)) {
			throw new AppException(OrderCodeConst.ORDER_HAD_TRADED);
		}
		if (tradeOrder.getStatus().equals(TradeCoinStatus.FAIL)) {
			throw new AppException(OrderCodeConst.ORDER_NOT_EXISTED);
		}
		TradeServcie tradeServcie = tradeServiceManager.getTradeService(tradeOrder.getSymbol());
		if (tradeServcie == null) {
			throw new AppException(MatchCodeConst.INVALID_SYMBOL);
		}
		try {
			tradeResult = tradeServcie.cancel(tradeOrder.getInnerOrderNo(),
					tradeOrder.getOrderType().equals(TradeCoinType.BUY) ? Type.BUY : Type.SELL);
		} catch (Exception e) {
			log.error("撮合引擎调用异常", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		matchOrderService.cancelOrder(apiRequestExtractDto.getUid(), tradeOrder.getInnerOrderNo(),
				tradeOrder.getSymbol(), tradeResult.getPrice(), tradeResult.getMarketOver(), tradeResult.getOverNum());

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

}
