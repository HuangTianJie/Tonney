package com.gop.asset.controller;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.gop.asset.dto.FinanceDetailDto;
import com.gop.asset.dto.FinanceDto;
import com.gop.asset.service.FinanceService;
import com.gop.coin.transfer.service.BrokerAssetOperService;
import com.gop.domain.Finance;
import com.gop.domain.FinanceDetail;
import com.gop.domain.enums.OptType;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController("managerBrokerAssetController")
@RequestMapping("/broker/asset")
@Slf4j
// @Api("经销商")
public class BrokerAssetController {

	@Autowired
	private FinanceService financeService;

	@Autowired
	private BrokerAssetOperService brokerAssetOperService;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/deposit", method = RequestMethod.GET)
	public void deposit(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {
		Integer operUid = context.getLoginSession().getUserId();
		
		//为券商用户充值资产
		brokerAssetOperService.brokerAssetOperDeposit(uid, assetCode, amount, operUid);
		
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/withdraw", method = RequestMethod.GET)
	public void withdraw(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {
		Integer operUid = context.getLoginSession().getUserId();
		
		//为券商用户提取资产
		brokerAssetOperService.brokerAssetOperWithdraw(uid, assetCode, amount, operUid);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})") })
	@RequestMapping(value = "/finances", method = RequestMethod.GET)
	public PageModel<FinanceDto> getBusinessFinance(@AuthForHeader AuthContext context,
			@RequestParam("brokerId") Integer brokerId, @RequestParam(value = "uid", required = false) Integer uid,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<Finance> pageInfo = financeService.queryFinanceByBroker(brokerId, uid, pageSize, pageNo);
		PageModel<FinanceDto> pageModel = new PageModel<>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setList(
				pageInfo.getList().stream().map(finance -> new FinanceDto(finance)).collect(Collectors.toList()));

		return pageModel;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})") })
	@RequestMapping(value = "/finance/details", method = RequestMethod.GET)
	public PageModel<FinanceDetailDto> getBusinessFinanceDetail(@AuthForHeader AuthContext context,
			@RequestParam("uid") Integer uid, @RequestParam(value = "type", required = false) OptType type,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<FinanceDetail> pageInfo = financeService.queryFinanceDetailByType(uid, type, pageSize, pageNo);
		PageModel<FinanceDetailDto> pageModel = new PageModel<>();

		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setList(
				pageInfo.getList().stream().map(finance -> new FinanceDetailDto(finance)).collect(Collectors.toList()));

		return pageModel;
	}

	
	
}
