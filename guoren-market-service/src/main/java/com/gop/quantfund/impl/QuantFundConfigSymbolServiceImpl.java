package com.gop.quantfund.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.QuantFundCodeConst;
import com.gop.domain.QuantFundAdmissionTicket;
import com.gop.domain.QuantFundConfig;
import com.gop.domain.QuantFundConfigSymbol;
import com.gop.domain.User;
import com.gop.domain.enums.AuthLevel;
import com.gop.domain.enums.QuantFundAdmissionTicketStatus;
import com.gop.domain.enums.QuantFundConfigSymbolStatus;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.domain.enums.TradeCoinType;
import com.gop.exception.AppException;
import com.gop.mapper.QuantFundConfigSymbolMapper;
import com.gop.match.dto.MatchOrderDto;
import com.gop.mode.vo.PageModel;
import com.gop.quantfund.QuantFundAdmissionTicketService;
import com.gop.quantfund.QuantFundConfigService;
import com.gop.quantfund.QuantFundConfigSymbolService;
import com.gop.user.facade.UserFacade;
import com.gop.util.BigDecimalUtils;
@Service("QuantFundConfigSymbolService")
public class QuantFundConfigSymbolServiceImpl implements QuantFundConfigSymbolService{
	@Autowired
	private QuantFundConfigSymbolMapper quantFundConfigSymbolMapper;
	
	@Autowired
	private UserAccountFacade userAccountFacade;
	
	@Autowired
	private UserFacade userfacade;
	
	@Autowired
	private QuantFundConfigService quantFundConfigService;
	
	@Autowired
	private QuantFundAdmissionTicketService quantFundAdmissionTicketService;
	
	@Override
	public int createOrUpdate(QuantFundConfigSymbol symbol) {
		return quantFundConfigSymbolMapper.insertOrUpdate(symbol);
	}

	@Override
	public QuantFundConfigSymbol selectBySymbol(String symbol) {
		return quantFundConfigSymbolMapper.selectBySymbol(symbol);
	}

	@Override
	public void checkQuantFundConfigSymbolOrder(String symbol, MatchOrderDto matchOrderDto) {
		QuantFundConfigSymbol quantFundConfigSymbol = selectBySymbol(symbol);
		//校验是否为基金交易对
		if (null == quantFundConfigSymbol) {
			return;
		}
		//基金币种
		String fundAssetCode = quantFundConfigSymbol.getFundAssetCode();
		User user = userfacade.getUser(matchOrderDto.getUserId());
		UserAccountDto userAccountDto = userAccountFacade.queryAccount(matchOrderDto.getUserId(),quantFundConfigSymbol.getFundAssetCode());
		//KYC数量限制 挂买单
		if (TradeCoinType.BUY.equals(matchOrderDto.getTradeCoinType())) {
			if (AuthLevel.LEVEL0.equals(user.getAuthLevel())) {
				BigDecimal amountLimit = new BigDecimal(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.KYCASSETCODELIMITED).getProfileValue());
				//基金总量为 可用+冻结+挂单数量
				BigDecimal totalFundAmount = userAccountDto.getAmountAvailable().add(userAccountDto.getAmountLock()).add(matchOrderDto.getAmount());
				if (BigDecimalUtils.isBiggerOrEqual(totalFundAmount, amountLimit)) {
					throw new AppException(QuantFundCodeConst.QUANT_FUND_KYC_ORDER_AMOUNT_LIMIT);
				}
			}
		}
		//基金交易对若在TOKEN SALE预售阶段 校验入场券
		if (QuantFundConfigSymbolStatus.PRELISTED.equals(quantFundConfigSymbol.getStatus())) {
			QuantFundAdmissionTicket quantFundAdmissionTicket = quantFundAdmissionTicketService.getQuantFundAdmissionTicketByUidAndFundAssetCode(matchOrderDto.getUserId(), fundAssetCode);
			if (null == quantFundAdmissionTicket 
					|| !QuantFundAdmissionTicketStatus.PURCHASED.equals(quantFundAdmissionTicket.getStatus())) {
				throw new AppException(QuantFundCodeConst.NO_QUANT_FUND_TIKET);
			} 
		}
	}

	@Override
	public PageModel<QuantFundConfigSymbol> getConfigSymbolPage(QuantFundConfigSymbolStatus status,
			String fundSymbol, Integer pageSize, Integer pageNo) {
		PageModel<QuantFundConfigSymbol> pageModel = new PageModel<>();
		PageInfo<QuantFundConfigSymbol> pageInfo = new PageInfo<>(quantFundConfigSymbolMapper.selectByFundSymbol(fundSymbol, status));
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(pageInfo.getList());
		return pageModel;
	}
}
