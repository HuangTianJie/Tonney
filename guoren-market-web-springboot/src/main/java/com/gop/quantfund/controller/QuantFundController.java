package com.gop.quantfund.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.QuantFundCodeConst;
import com.gop.domain.Finance;
import com.gop.domain.QuantFundAdmissionTicket;
import com.gop.domain.QuantFundConfigSymbol;
import com.gop.domain.QuantFundTicketPreReturnTemp;
import com.gop.domain.User;
import com.gop.domain.enums.AssetStatus;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.exception.AppException;
import com.gop.quantfund.QuantFundAdmissionTicketService;
import com.gop.quantfund.QuantFundConfigService;
import com.gop.quantfund.QuantFundConfigSymbolService;
import com.gop.quantfund.QuantFundService;
import com.gop.quantfund.QuantFundTicketPreReturnTempService;
import com.gop.quantfund.dto.QuantFundInfoQueryDto;
import com.gop.quantfund.dto.QuantFundQueryDto;
import com.gop.quantfund.dto.QuantFundSymbolQueryDto;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController
@RequestMapping("/quantfund")
public class QuantFundController {

	@Autowired
	private QuantFundAdmissionTicketService quantFundAdmissionTicketService;

	@Autowired
	private QuantFundService quantFundService;

	@Autowired
	private QuantFundConfigService quantFundConfigService;

	@Autowired
	private ConfigAssetService configAssetService;
	

	@Autowired
	private UserFacade userFacade;
	
	@Autowired
	private FinanceService financeService;
	
	@Autowired
	private QuantFundConfigSymbolService quantFundConfigSymbolService;
	
	private final String UNPURCHASED = "UNPURCHASED";
	
	@Autowired
	private QuantFundTicketPreReturnTempService quantFundTicketPreReturnTempService;

	// 入场券购买
	@RequestMapping(value = "/ticket-buy", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})") })
	public void ticketBuy(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode) {
		Integer uid = context.getLoginSession().getUserId();
		quantFundAdmissionTicketService.buyQuantFundAdmissionTicket(uid, fundAssetCode);
	}

	// 入场券退还
	@RequestMapping(value = "/ticket-return", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public void ticketReturn(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode) {
		Integer uid = context.getLoginSession().getUserId();
		quantFundAdmissionTicketService.returnQuantFundAdmissionTicket(uid, fundAssetCode);
	}
	
	// 入场券提前退还
	@RequestMapping(value = "/ticket-prereturn", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public void ticketPreReturn(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode) {
		Integer uid = context.getLoginSession().getUserId();
		quantFundAdmissionTicketService.preReturnQuantFundAdmissionTicket(uid, fundAssetCode);
	}

	// 量化基金查询
	@RequestMapping(value = "/info-query", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public QuantFundInfoQueryDto infoQuery(@AuthForHeader AuthContext context,
			@RequestParam("fundAssetCode") String fundAssetCode) {
		if (!configAssetService.validateAssetConfig(fundAssetCode, AssetStatus.LISTED)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		Long fundEndDate = Long.valueOf(quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDENDDATE).getProfileValue());
		Long fundBeginDate = Long.valueOf(quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDBEGINDATE).getProfileValue());
		String ticketStatus = quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETSTATUS).getProfileValue();
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		QuantFundAdmissionTicket ticket = quantFundAdmissionTicketService
				.getQuantFundAdmissionTicketByUidAndFundAssetCode(uid, fundAssetCode);
		Finance finance = financeService.queryAccount(uid, fundAssetCode);
		QuantFundInfoQueryDto dto = new QuantFundInfoQueryDto();
		dto.setUserAccount(user.getEmail());
		dto.setFundAssetCode(fundAssetCode);
		dto.setLevel(user.getAuthLevel());
		dto.setFundBeginDate(fundBeginDate);
		dto.setFundEndDate(fundEndDate);
		if (null==finance) {
			dto.setFundAssetCodeAmount(BigDecimal.ZERO);
		}else {
			dto.setFundAssetCodeAmount(finance.getAmountAvailable().add(finance.getAmountLock()));
		}
		dto.setTicketStatus(ticketStatus);
		if (null == ticket) {
			dto.setBuyTicket(UNPURCHASED);
			return dto;
		}
		dto.setBuyTicket(ticket.getStatus().toString());
		QuantFundTicketPreReturnTemp quantFundTicketPreReturnTemp  = quantFundTicketPreReturnTempService.getQuantFundTicketPreReturnTemp(uid, fundAssetCode);
		dto.setPreReturn(null != quantFundTicketPreReturnTemp);
		return dto;
	}

	// 量化基金查询
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public QuantFundQueryDto infoQuery(@RequestParam("fundAssetCode") String fundAssetCode) {
		if (!configAssetService.validateAssetConfig(fundAssetCode, AssetStatus.LISTED)) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		Long fundEndDate = Long.valueOf(quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDENDDATE).getProfileValue());
		Long fundBeginDate = Long.valueOf(quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDBEGINDATE).getProfileValue());
		QuantFundQueryDto dto = new QuantFundQueryDto();
		dto.setFundBeginDate(fundBeginDate);
		dto.setFundEndDate(fundEndDate);
		dto.setTicketStatus(quantFundConfigService
				.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETSTATUS).getProfileValue());
		return dto;
	}

	// 基金收益领取
	@RequestMapping(value = "/income-receive", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})") })
	public void incomeReceive(@AuthForHeader AuthContext context, @RequestParam("fundAssetCode") String fundAssetCode) {
		Integer uid = context.getLoginSession().getUserId();
		quantFundService.receiveQuantFundIncome(uid, fundAssetCode);
	}
	// 量化交易对状态及入场券信息查询
	@RequestMapping(value = "/symbol-query", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public QuantFundSymbolQueryDto symbolQuery(@AuthForHeader AuthContext context,@RequestParam("fundAssetCode") String fundAssetCode,@RequestParam("fundSymbol")String fundSymbol) {
		Integer uid = context.getLoginSession().getUserId();
		QuantFundSymbolQueryDto quantFundSymbolQueryDto = new QuantFundSymbolQueryDto();
		QuantFundAdmissionTicket ticket = quantFundAdmissionTicketService
				.getQuantFundAdmissionTicketByUidAndFundAssetCode(uid, fundAssetCode);
		QuantFundConfigSymbol symbol = quantFundConfigSymbolService.selectBySymbol(fundSymbol);
		if (null == symbol) {
			quantFundSymbolQueryDto.setSymbolStatus(null);
		}else {
			quantFundSymbolQueryDto.setSymbolStatus(symbol.getStatus());
		}
		if (null == ticket) {
			quantFundSymbolQueryDto.setTicketStatus(UNPURCHASED);
		}else {
			quantFundSymbolQueryDto.setTicketStatus(ticket.getStatus().toString());
		}
		return quantFundSymbolQueryDto;
	}
}