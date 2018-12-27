package com.gop.match.facade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gop.domain.ConfigAsset;
import com.gop.domain.enums.CurrencyType;
import com.gop.match.dto.FeeDto;
import com.gte.domain.sector.enums.Sector;
import com.gte.sector.service.ConfigSectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.TradeMatchResult;
import com.gop.domain.TradeOrder;
import com.gop.domain.enums.ConfigSymbolType;
import com.gop.domain.enums.TradeCoinType;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.match.service.ConfigSymbolProfileService;
import com.gop.match.service.MatchOrderService;
import com.gop.match.service.SymbolService;
import com.gop.match.service.TradeRecordService;
import com.gop.match.service.UserTransactionFeeWhiteListService;
import com.gop.util.BigDecimalUtils;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Service
@Slf4j
public class MatchTradeFacadeImpl implements MatchTradeFacade {
	@Autowired
	private SymbolService symbolService;

	@Autowired
	private ConfigSymbolProfileService configSymbolProfileService;
	@Autowired
	private UserAccountFacade userAccountFacade;

	@Autowired
	private TradeRecordService tradeRecordService;

	@Autowired
	private MatchOrderService matchOrderService;

	@Autowired
	private UserTransactionFeeWhiteListService userTransactionFeeWhiteListService;


	@Resource
	private ConfigSectorService configSectorService;

	@Override
	@Transactional
	public void matchRecord(String buyRequestNo, String sellRequestNo, Integer buyId, Integer sellId, BigDecimal num,
			BigDecimal price, String symbol) {

		TradeOrder buyTradeOrder = matchOrderService.queryTradeOrderInnerOrderNo(buyRequestNo);
		TradeOrder sellTradeOrder = matchOrderService.queryTradeOrderInnerOrderNo(sellRequestNo);
		// 生成撮合成功买入订单号
		String buySuccessOrder = SequenceUtil.getNextId();
		// 生成撮合成功卖出订单号
		String sellSuccessOrder = SequenceUtil.getNextId();
		TradeMatchResult tradeResult = new TradeMatchResult();
		tradeResult.setBuyInnerOrderNo(buyRequestNo);
		tradeResult.setSellInnerOrderNo(sellRequestNo);
		tradeResult.setBuyRequestNo(buySuccessOrder);
		tradeResult.setSellRequestNo(sellSuccessOrder);
		tradeResult.setBuyUid(buyId);
		tradeResult.setSellUid(sellId);
		tradeResult.setSymbol(symbol);
		tradeResult.setBuyBrokerId(buyTradeOrder.getBrokerId());
		tradeResult.setSellBrokerId(sellTradeOrder.getBrokerId());
		tradeResult.setNumber(num);
		tradeResult.setPrice(price);

		tradeResult.setCreateTime(new Date());
		//计算 买入下单冻结的手续费
		FeeDto buyOrderFeeDto = new FeeDto();
		//计算 买入成交手续费
		FeeDto buyFeeDto = getBuyFee(buyId, num, tradeResult.getPrice(), tradeResult.getSymbol());
		if(buyFeeDto.getIsCollect()){
			buyOrderFeeDto = getBuyFee(buyId, num, buyTradeOrder.getPrice(), tradeResult.getSymbol());
		}
		BigDecimal buyFee = buyFeeDto.getFee();
		//计算 卖出手续费
		FeeDto sellFeeDto = getSellFee(sellId, num, tradeResult.getPrice(), tradeResult.getSymbol());
		BigDecimal sellFee = sellFeeDto.getFee();
		tradeResult.setBuyFee(buyFee);
		tradeResult.setBuyFeeAssetCode(buyFeeDto.getAssetCode());
		tradeResult.setSellFee(sellFee);
		tradeResult.setSellFeeAssetCode(buyFeeDto.getAssetCode());

		try {
			CreateTradeMatchResult(tradeResult);
		} catch (DuplicateKeyException e) {
			log.info("收到重复的消息:{}", tradeResult);
			return;
		}
		List<AssetOperationDto> operationDtos = null;
		if (buyId > sellId) {
			operationDtos = updateMatchOrder(buyId, buyRequestNo, buySuccessOrder, symbol, TradeCoinType.BUY, num,
					price, buyFeeDto,buyOrderFeeDto);
			operationDtos.addAll(updateMatchOrder(sellId, sellRequestNo, sellSuccessOrder, symbol, TradeCoinType.SELL,
					num, price, sellFeeDto,buyOrderFeeDto));
		} else {
			operationDtos = updateMatchOrder(sellId, sellRequestNo, sellSuccessOrder, symbol, TradeCoinType.SELL, num,
					price, sellFeeDto,buyOrderFeeDto);
			operationDtos.addAll(updateMatchOrder(buyId, buyRequestNo, buySuccessOrder, symbol, TradeCoinType.BUY, num,
					price, buyFeeDto,buyOrderFeeDto));
		}
		transactionAccount(operationDtos);
	}

	/**
	 * 获取 卖方 成交手续费
	 * @param num 成交数量
	 * @param price 成交价格
	 * @param symbol 交易对
	 * @return 卖出手续费
	 */
	public FeeDto getSellFee(Integer sellUid,BigDecimal num,  BigDecimal price,String symbol) {
		FeeDto feeDto = FeeDto.builder().build();
		ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(symbol);
		if (!userTransactionFeeWhiteListService.checkUserinWhiteList(sellUid)) {

			BigDecimal sellFeeRate;
			BigDecimal sellMinfee;
			List<ConfigAsset> list = configSectorService.getAssetCodeBySector(Sector.INNOVATE, configSymbol.getAssetCode2(),null);
			if (list.size() == 0) {
				sellFeeRate = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETFEERATE);
				sellMinfee = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETMINFEE);
			}else{
				sellFeeRate = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETSELLFEERATE);
				sellMinfee = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETSELLMINFEE);
			}
			BigDecimal fee = num.multiply(price).multiply(sellFeeRate);
			BigDecimal sellFee = BigDecimalUtils.isBiggerOrEqual(fee, sellMinfee) ? fee : sellMinfee;
			feeDto.setFee(sellFee);
			feeDto.setAssetCode(configSymbol.getAssetCode1());
			return feeDto;
		}else{
			feeDto.setFee(BigDecimal.ZERO);
			return feeDto;
		}
	}

	/**
	 * 获取 买方 成交/下单价手续费
	 * @param num 成交数量
	 * @param price 成交价格/下单价
	 * @param symbol 交易对
	 * @return 买方手续费
	 */
	public FeeDto getBuyFee(Integer buyUid, BigDecimal num, BigDecimal price,String symbol) {
		FeeDto feeDto = FeeDto.builder().build();
		if (!userTransactionFeeWhiteListService.checkUserinWhiteList(buyUid)) {
			BigDecimal buyFee;
			BigDecimal buyFeeRate;
			BigDecimal buyMinfee;
			BigDecimal fee;
			ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(symbol);
			List<ConfigAsset> list = configSectorService.getAssetCodeBySector(Sector.INNOVATE, configSymbol.getAssetCode2(),null);
			if (list.size() == 0) {
				//在主板中，手续费收计价币。
				buyFeeRate = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETFEERATE);
				buyMinfee = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETMINFEE);
				fee = num.multiply(buyFeeRate);
				feeDto.setAssetCode(configSymbol.getAssetCode2());
				feeDto.setIsCollect(false);
				feeDto.setBuyFeeRate(buyFeeRate);
			} else {
				//在创新中，手续费收交易币
				buyFeeRate = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETBUYFEERATE);
				buyMinfee = configSymbolProfileService.getBigDecimalValue(symbol,
						ConfigSymbolType.ASSETBUYMINFEE);
				fee = num.multiply(price).multiply(buyFeeRate);
				feeDto.setAssetCode(configSymbol.getAssetCode1());
				feeDto.setIsCollect(true);
				feeDto.setBuyFeeRate(buyFeeRate);
			}

			buyFee = BigDecimalUtils.isBiggerOrEqual(fee, buyMinfee) ? fee : buyMinfee;
			feeDto.setFee(buyFee);
			return feeDto;
		}else{
			feeDto.setFee(BigDecimal.ZERO);
			return feeDto;
		}
	}

	private void CreateTradeMatchResult(TradeMatchResult tradeResult) {
		log.info("" + tradeResult);
		tradeRecordService.insert(tradeResult);

	}

	/**
	 * 
	 * @param uid
	 * @param symbol
	 * @param num
	 * @param price
	 * @return 退款金额
	 */
	private List<AssetOperationDto> updateMatchOrder(int uid, String innerNo, String requestNo, String symbol,
			TradeCoinType cointype, BigDecimal num, BigDecimal price, FeeDto feeDto, FeeDto buyOrderFeeDto) {

		ConfigSymbol configSymbol = symbolService.getConfigSymbolBYSymbol(symbol);

		if (cointype.equals(TradeCoinType.BUY)) {

			BigDecimal unlock = matchOrderService.updateMatchOrder(innerNo, price, num, feeDto.getFee());

			return buildMatchOrderBuyAssetOperationDto(uid, requestNo, configSymbol.getAssetCode1(),
					configSymbol.getAssetCode2(), num, price, unlock, feeDto,buyOrderFeeDto);
		} else {

			BigDecimal unlock = matchOrderService.updateMatchOrder(innerNo, price, num, feeDto.getFee());
			return buildMatchOrderSellAssetOperationDto(uid, requestNo, configSymbol.getAssetCode1(),
					configSymbol.getAssetCode2(), num, price, unlock, feeDto);
		}

	}

	/**
	 * 买方 解冻 手续费
	 * @param uid
	 * @param requestNo
	 * @param assetCode1
	 * @param assetCode2
	 * @param num
	 * @param price
	 * @param unlock
	 * @param feeDto
	 * @return
	 */
	private List<AssetOperationDto> buildMatchOrderBuyAssetOperationDto(int uid, String requestNo, String assetCode1,
			String assetCode2, BigDecimal num, BigDecimal price, BigDecimal unlock, FeeDto feeDto, FeeDto buyOrderFeeDto) {
		List<AssetOperationDto> lists = new ArrayList<>();
		//log.info("num:"+num+ ",price:" + price+",unlock: " +unlock+",feeDto:" + feeDto +",buyOrderFeeDto:" + buyOrderFeeDto);
		BigDecimal buyFee = buyOrderFeeDto.getIsCollect()? buyOrderFeeDto.getFee() : BigDecimal.ZERO;
		if(!BigDecimal.ZERO.equals(buyFee)){
			AssetOperationDto assetOperationDtoRestlock = new AssetOperationDto();
			assetOperationDtoRestlock.setAccountClass(AccountClass.EXPENSE);
			assetOperationDtoRestlock.setAccountSubject(AccountSubject.MATCH_OUT);
			assetOperationDtoRestlock.setBusinessSubject(BusinessSubject.BUY_FEE_RETURN);
			assetOperationDtoRestlock.setAssetCode(buyOrderFeeDto.getAssetCode());
			assetOperationDtoRestlock.setLockAmount(buyFee.negate());
			assetOperationDtoRestlock.setAmount(buyFee);
			assetOperationDtoRestlock.setUid(uid);
			assetOperationDtoRestlock.setLoanAmount(BigDecimal.ZERO);
			assetOperationDtoRestlock.setMemo("恢复-预买入手续费");
			assetOperationDtoRestlock.setRequestNo(requestNo);
			assetOperationDtoRestlock.setIndex(0);
			lists.add(assetOperationDtoRestlock);
		}



		AssetOperationDto assetOperationDtoUnlock = new AssetOperationDto();
		assetOperationDtoUnlock.setAccountClass(AccountClass.EXPENSE);
		assetOperationDtoUnlock.setAccountSubject(AccountSubject.MATCH_OUT);
		assetOperationDtoUnlock.setBusinessSubject(BusinessSubject.MATCH_SELL);
		assetOperationDtoUnlock.setAssetCode(assetCode1);
		assetOperationDtoUnlock.setLockAmount(unlock.add(price.multiply(num)).negate());
		assetOperationDtoUnlock.setAmount(unlock);
		assetOperationDtoUnlock.setUid(uid);
		assetOperationDtoUnlock.setLoanAmount(BigDecimal.ZERO);
		assetOperationDtoUnlock.setMemo("解冻");
		assetOperationDtoUnlock.setRequestNo(requestNo);
		assetOperationDtoUnlock.setIndex(1);
		lists.add(assetOperationDtoUnlock);

		// 获得
		AssetOperationDto assetOperationDtoAc = new AssetOperationDto();
		assetOperationDtoAc.setAccountClass(AccountClass.INCOME);
		assetOperationDtoAc.setAccountSubject(AccountSubject.MATCH_IN);
		assetOperationDtoAc.setBusinessSubject(BusinessSubject.MATCH_BUY);
		assetOperationDtoAc.setAssetCode(assetCode2);
		assetOperationDtoAc.setAmount(num);
		assetOperationDtoAc.setLoanAmount(BigDecimal.ZERO);
		assetOperationDtoAc.setLockAmount(BigDecimal.ZERO);
		assetOperationDtoAc.setUid(uid);
		assetOperationDtoAc.setMemo("买入获得");
		assetOperationDtoAc.setRequestNo(requestNo);
		assetOperationDtoAc.setIndex(2);
		lists.add(assetOperationDtoAc);

		// 手续费
		if (!BigDecimalUtils.isEqualZero(feeDto.getFee())) {
			AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
			assetOperationDtoFee.setAccountClass(AccountClass.INCOME);
			assetOperationDtoFee.setAccountSubject(AccountSubject.FEE_MATCH_SPEND);
			assetOperationDtoFee.setBusinessSubject(BusinessSubject.FEE);
			assetOperationDtoFee.setAssetCode(feeDto.getAssetCode());
			assetOperationDtoFee.setLockAmount(BigDecimal.ZERO);
			assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
			assetOperationDtoFee.setAmount(feeDto.getFee().negate());
			assetOperationDtoFee.setUid(uid);
			assetOperationDtoFee.setMemo("买入手续费");
			assetOperationDtoFee.setRequestNo(requestNo);
			assetOperationDtoFee.setIndex(3);
			lists.add(assetOperationDtoFee);
		}
		//log.info("=buy="+lists);
		return lists;
	}

	/**
	 * 卖方 解冻 手续费
	 * @param uid
	 * @param requestNo
	 * @param assetCode1
	 * @param assetCode2
	 * @param num
	 * @param price
	 * @param unlock
	 * @param feeDto
	 * @return
	 */
	private List<AssetOperationDto> buildMatchOrderSellAssetOperationDto(int uid, String requestNo, String assetCode1,
			String assetCode2, BigDecimal num, BigDecimal price, BigDecimal unlock, FeeDto feeDto) {
		List<AssetOperationDto> lists = new ArrayList<>();

		AssetOperationDto assetOperationDtoUnlock = new AssetOperationDto();
		assetOperationDtoUnlock.setAccountClass(AccountClass.EXPENSE);
		assetOperationDtoUnlock.setAccountSubject(AccountSubject.MATCH_OUT);
		assetOperationDtoUnlock.setBusinessSubject(BusinessSubject.MATCH_SELL);
		assetOperationDtoUnlock.setAssetCode(assetCode2);
		assetOperationDtoUnlock.setLockAmount(num.negate());
		assetOperationDtoUnlock.setAmount(BigDecimal.ZERO);
		assetOperationDtoUnlock.setLoanAmount(BigDecimal.ZERO);
		assetOperationDtoUnlock.setUid(uid);
		assetOperationDtoUnlock.setMemo("卖出解冻");
		assetOperationDtoUnlock.setRequestNo(requestNo);
		assetOperationDtoUnlock.setIndex(0);
		lists.add(assetOperationDtoUnlock);
		// 获得
		AssetOperationDto assetOperationDtoAc = new AssetOperationDto();
		assetOperationDtoAc.setAccountClass(AccountClass.INCOME);
		assetOperationDtoAc.setAccountSubject(AccountSubject.MATCH_IN);
		assetOperationDtoAc.setBusinessSubject(BusinessSubject.MATCH_BUY);
		assetOperationDtoAc.setAssetCode(assetCode1);
		assetOperationDtoAc.setAmount(num.multiply(price));
		assetOperationDtoAc.setLoanAmount(BigDecimal.ZERO);
		assetOperationDtoAc.setLockAmount(BigDecimal.ZERO);
		assetOperationDtoAc.setUid(uid);
		assetOperationDtoAc.setMemo("卖出获得");
		assetOperationDtoAc.setRequestNo(requestNo);
		assetOperationDtoAc.setIndex(1);
		lists.add(assetOperationDtoAc);

		// 手续费
		if (!BigDecimalUtils.isEqualZero(feeDto.getFee())) {
			AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
			assetOperationDtoFee.setAccountClass(AccountClass.LIABILITY);
			assetOperationDtoFee.setAccountSubject(AccountSubject.FEE_MATCH_SPEND);
			assetOperationDtoFee.setBusinessSubject(BusinessSubject.FEE);
			assetOperationDtoFee.setAssetCode(feeDto.getAssetCode());
			assetOperationDtoFee.setLockAmount(BigDecimal.ZERO);
			assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
			assetOperationDtoFee.setAmount(feeDto.getFee().negate());
			assetOperationDtoFee.setUid(uid);
			assetOperationDtoFee.setMemo("卖出手续费");
			assetOperationDtoFee.setRequestNo(requestNo);
			assetOperationDtoFee.setIndex(2);
			lists.add(assetOperationDtoFee);
		}
		//log.info("=sell="+lists);
		return lists;
	}

	private void transactionAccount(List<AssetOperationDto> assetOperationDtos) {
		userAccountFacade.assetOperation(assetOperationDtos);
	}

}
