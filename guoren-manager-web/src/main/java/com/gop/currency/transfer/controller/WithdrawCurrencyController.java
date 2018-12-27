package com.gop.currency.transfer.controller;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.gop.config.CommonConfig;
import com.gop.currency.transfer.dto.WithdrawOrderQueryDetailDto;
import com.gop.currency.transfer.service.ChannelCurrencyWithdrawOrderService;
import com.gop.currency.transfer.service.UnCerfWithdrawCurrencyService;
import com.gop.currency.transfer.service.WithdrawCurrencyService;
import com.gop.currency.withdraw.gateway.facade.PayFacadeService;
import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.domain.enums.WithdrawCurrencyPayMode;
import com.gop.mode.vo.PageModel;
import com.gop.util.OrderUtil;
import com.gop.util.SequenceUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("managerWithdrawCurrencyController")
@RequestMapping("/currency/withdraw")
public class WithdrawCurrencyController {

	// @Autowired
	// private ChannelWithdrawOrderService withdrawOrderService;

	@Autowired
	private WithdrawCurrencyService withdrawService;
	
	@Autowired
	private UnCerfWithdrawCurrencyService unCerfWithdrawCurrencyService;
	
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/uncerf", method = RequestMethod.GET)
	public void withdrawCurrency(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {

		String txid = SequenceUtil.getNextId();
		
		unCerfWithdrawCurrencyService.currencyWithdrawOrderUnCerf(uid, txid, assetCode, amount);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	public PageModel<WithdrawOrderQueryDetailDto> transfer(@AuthForHeader AuthContext context,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "internalOrderId", required = false) String internalOrderId,
			@RequestParam(value = "accountName", required = false) String name,
			@RequestParam(value = "status", required = false) WithdrawCurrencyOrderStatus status,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {

		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}

		PageInfo<WithdrawCurrencyOrderUser> pageInfo = withdrawService.queryOrder(id, brokerId, uId,internalOrderId, account, name,
				status, pageNo, pageSize);

		PageModel<WithdrawOrderQueryDetailDto> pages = new PageModel<>();

		pages.setPageNo(pageNo);
		pages.setPageSize(pageSize);
		pages.setPageNum(pageInfo.getPages());
		pages.setTotal(pageInfo.getTotal());
		pages.setList(pageInfo.getList().stream().map(order -> new WithdrawOrderQueryDetailDto(order))
				.collect(Collectors.toList()));

		return pages;
	}

	/**
	 * 锁定
	 * 
	 * @param id
	 * @param ip
	 * @param request
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/lock", method = RequestMethod.GET)
	public void lockTransfer(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		int adminId = 1;
		withdrawService.lock(id, adminId);
	}

	/**
	 * 解锁
	 * 
	 * @param id
	 * @param ip
	 * @param request
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/unlock", method = RequestMethod.GET)
	public void unlockTransfer(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		int adminId = 1;
		withdrawService.unlock(id, adminId);
	}

	/**
	 * 撤销
	 * 
	 * @param id
	 * @param ip
	 * @param request
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/cancel", method = RequestMethod.GET)
	public void cancelTransfer(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		// int adminId = context.getLoginSession().getUserId();
		int adminId = 1;
		withdrawService.cancel(id, adminId);
	}

	/**
	 * 退款
	 * 
	 * @param id
	 * @param password
	 * @param ip
	 * @param request
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/refund", method = RequestMethod.GET)
	public void refundTransfer(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		int adminId = context.getLoginSession().getUserId();
//		int adminId = 1;
		withdrawService.refund(id, adminId);
	}

	/**
	 * 
	 * 统一用户提现通道
	 * 
	 * @author zhangli
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/pay", method = RequestMethod.GET)
	public void pay(@AuthForHeader AuthContext context, @RequestParam("payMode") String payMode,
			@RequestParam("id") Integer id) {
		// int adminId = context.getLoginSession().getUserId();
		int adminId = 1;
		withdrawService.withdrawByChannel(id, payMode, adminId);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/confirm", method = RequestMethod.GET)
	public void confirm(@AuthForHeader AuthContext context,
			@RequestParam("id") Integer id) {
		// int adminId = context.getLoginSession().getUserId();
		int adminId = 1;
		withdrawService.confirmOffline(id, adminId);
	}
}
