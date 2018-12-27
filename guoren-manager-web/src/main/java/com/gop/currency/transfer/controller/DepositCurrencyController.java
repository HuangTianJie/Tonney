package com.gop.currency.transfer.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.RechargeCodeConst;
import com.gop.config.CommonConfig;
import com.gop.currency.transfer.dto.DepositBankOrderDetailDto;
import com.gop.currency.transfer.dto.DepositMatchQueryDetailDto;
import com.gop.currency.transfer.dto.DepositOrderQueryDetailDto;
import com.gop.currency.transfer.service.DepositCurrencyQueryOrderService;
import com.gop.currency.transfer.service.DepositCurrencyService;
import com.gop.currency.transfer.service.DepositMatchBankOrderUserService;
import com.gop.currency.transfer.service.DepositMatchCurrencyOrderUserService;
import com.gop.currency.transfer.service.ImportExcelRecord;
import com.gop.currency.transfer.service.UnCerfDepositCurrencyOrderService;
import com.gop.domain.DepositCurrencyOrderUser;
import com.gop.domain.DepositMatchBankOrderUser;
import com.gop.domain.DepositMatchCurrencyOrderUser;
import com.gop.domain.enums.MatchState;
import com.gop.domain.enums.RechargeStatus;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.ImportExcelResultVo;
import com.gop.mode.vo.PageModel;
import com.gop.util.SequenceUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController("managerDepositCurrencyController")
@RequestMapping("/currency/deposit")
@Slf4j
public class DepositCurrencyController {

	@Autowired
	private DepositCurrencyQueryOrderService depositCurrencyOrderService;

	@Autowired
	private DepositCurrencyService depositService;

	@Autowired
	private DepositMatchBankOrderUserService depositMatchBankOrderUserService;

	@Autowired
	DepositMatchCurrencyOrderUserService depositMatchCurrencyOrderUserService;

	@Autowired
	@Qualifier("aliPay")
	ImportExcelRecord importAliPayExcelRecord;

	@Autowired
	@Qualifier("cibPay")
	ImportExcelRecord importCibPayExcelRecord;

	@Autowired
	private UnCerfDepositCurrencyOrderService unCerfDepositCurrencyOrderService;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/uncerf", method = RequestMethod.GET)
	public void depositCurrency(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {

		String txid = SequenceUtil.getNextId();
		unCerfDepositCurrencyOrderService.currencyDepositOrderUnCerf(uid, txid, assetCode, amount);

	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	public PageModel<DepositOrderQueryDetailDto> transfer(@AuthForHeader AuthContext context,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "orderNo", required = false) String orderNo,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "status", required = false) WithdrawCurrencyOrderStatus status) {

		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}

		PageInfo<DepositCurrencyOrderUser> pageInfo = depositCurrencyOrderService.queryOrder(id, brokerId, uId, orderNo,
				null, name, account, status, pageNo, pageSize);

		PageModel<DepositOrderQueryDetailDto> pages = new PageModel<>();

		pages.setPageNo(pageNo);
		pages.setPageSize(pageSize);
		pages.setPageNum(pageInfo.getPages());
		pages.setTotal(pageInfo.getTotal());
		pages.setList(pageInfo.getList().stream().map(order -> new DepositOrderQueryDetailDto(order))
				.collect(Collectors.toList()));
		return pages;
	}

	@SuppressWarnings("unchecked")
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/import-order-list", method = RequestMethod.GET)
	public PageModel<DepositBankOrderDetailDto> bankOrderList(@AuthForHeader AuthContext context,
			@RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "minAmount", required = false) BigDecimal minAmount,
			@RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount,
			@RequestParam(value = "status", required = false) RechargeStatus rechargeStatus,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "accountNo", required = false) String accountNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNo", required = false) Integer pageNo) {
		List<DepositMatchBankOrderUser> pageInfo = depositMatchBankOrderUserService.queryBankStatement(beginTime,
				endTime, minAmount, maxAmount, rechargeStatus, name, accountNo, pageNo, pageSize);
		PageModel<DepositBankOrderDetailDto> pages = new PageModel<DepositBankOrderDetailDto>();
		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}

		pages.setPageNo(pageNo);
		pages.setPageSize(pageSize);
		pages.setPageNum(((Page<DepositMatchBankOrderUser>) pageInfo).getPages());
		pages.setTotal(((Page<DepositMatchBankOrderUser>) pageInfo).getTotal());
		pages.setList(((Page<DepositMatchBankOrderUser>) pageInfo).stream()
				.map(order -> new DepositBankOrderDetailDto(order)).collect(Collectors.toList()));
		return pages;
	}

	/**
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/match-query", method = RequestMethod.GET)
	public List<DepositMatchQueryDetailDto> matchQuery(@AuthForHeader AuthContext context,
			@RequestParam("id") Integer id) {
		DepositMatchBankOrderUser depositMatchBankOrderUser = depositMatchBankOrderUserService.query(id);
		if (null == depositMatchBankOrderUser) {
			throw new AppException(RechargeCodeConst.MATCH_ORDER_NOT_EXIST);
		}
		List<DepositCurrencyOrderUser> lists = depositCurrencyOrderService.queryOrder(null,
				depositMatchBankOrderUser.getAccountNo(), WithdrawCurrencyOrderStatus.WAIT);
		if (null == lists || lists.isEmpty()) {
			return new ArrayList<>();
		}
		return lists.stream().map(a -> new DepositMatchQueryDetailDto(a)).collect(Collectors.toList());
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/match", method = RequestMethod.GET)
	public void match(@AuthForHeader AuthContext context, @RequestParam("orderId") Integer orderId,
			@RequestParam("bankId") Integer bankId) {
		depositMatchCurrencyOrderUserService.match(orderId, bankId, context.getLoginSession().getUserId());

	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/match-orders", method = RequestMethod.GET)
	public PageModel<DepositMatchCurrencyOrderUser> matchOrderList(@AuthForHeader AuthContext context,
			@RequestParam(value = "status", required = false) MatchState status,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "accountNo", required = false) String accountNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNo", required = false) Integer pageNo) {
		if (pageNo == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
		}
		if (pageSize == null) {
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		List<DepositMatchCurrencyOrderUser> lists = depositMatchCurrencyOrderUserService.getMatchOrderByStatus(status,
				pageSize, pageNo, name, accountNo);

		PageModel<DepositMatchCurrencyOrderUser> pages = new PageModel<DepositMatchCurrencyOrderUser>();
		pages.setPageNo(pageNo);
		pages.setPageSize(pageSize);
		pages.setPageNum(((Page) lists).getPages());
		pages.setTotal(((Page) lists).getTotal());
		pages.setList(lists);
		return pages;
	}

	/**
	 * 充值订单直接确认到账功能
	 * 
	 * @param context
	 * @param id
	 * @param password
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/match/confirm", method = RequestMethod.GET)
	public void confirmMatch(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		depositMatchCurrencyOrderUserService.confirm(id, context.getLoginSession().getUserId());

	}

	/**
	 * 充值订单直接确认到账功能
	 * 
	 * @param context
	 * @param id
	 * @param password
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/order/confirm", method = RequestMethod.GET)
	public void confirmOrder(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {

		depositService.confirm(id, context.getLoginSession().getUserId());

	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/match/cancel", method = RequestMethod.GET)
	public void cancelMatch(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {
		depositMatchCurrencyOrderUserService.cancleMatch(id, context.getLoginSession().getUserId());

	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/upload-bank-file/{source}", method = RequestMethod.POST)
	public ImportExcelResultVo uploadBankFile(@AuthForHeader AuthContext context,
			@RequestParam("file") MultipartFile file, @PathVariable("source") String source) {

		InputStream fileInputStream = null;
		ImportExcelResultVo importExcelResultVo = null;
		if (null == file) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		try {
			fileInputStream = file.getInputStream();
			String fileName = file.getOriginalFilename();
			if (source.equals("alipay")) {
				importExcelResultVo = importAliPayExcelRecord.importRecordExcel(fileInputStream, fileName,
						context.getLoginSession().getUserId());
			} else if (source.equals("cib")) {
				importExcelResultVo = importCibPayExcelRecord.importRecordExcel(fileInputStream, fileName,
						context.getLoginSession().getUserId());
			} else {
				throw new AppException(CommonCodeConst.FIELD_ERROR);
			}
		} catch (Exception e) {
			log.error("上传文件异常,{}", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		} finally {
			if (null != fileInputStream) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return importExcelResultVo;
	}

}
