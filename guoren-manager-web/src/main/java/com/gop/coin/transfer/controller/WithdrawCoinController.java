package com.gop.coin.transfer.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.coin.transfer.dto.WithdrawDetailDto;
import com.gop.coin.transfer.service.ChannelCoinAddressDepositPoolService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinService;
import com.gop.domain.ChannelCoinAddressDepositPool;
import com.gop.domain.WithdrawCoinOrderUser;
import com.gop.domain.enums.WithdrawCoinOrderStatus;
import com.gop.domain.enums.WithdrawCoinOrderType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.util.SequenceUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController("managerWithdrawCoinController")
@RequestMapping("/withdraw/coin")
@Slf4j
public class WithdrawCoinController {

	@Autowired
	ConfigAssetService configAssetService;

	@Autowired
	private WithdrawCoinQueryService withdrawCoinQueryService;

	@Autowired
	private ChannelCoinAddressDepositPoolService channelCoinAddressDepositPoolService;


	@Autowired
	private WithdrawCoinService withdrawCoinService;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/uncerf", method = RequestMethod.GET)
	public void withdrawCoin(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {

		String txid = SequenceUtil.getNextId();

		withdrawCoinService.withdrawCoinOrderUnCerf(uid, txid, assetCode, amount);

	}

	/**
	 * * 转果仁记录查询
	 *
	 * @param id
	 * @param uId
	 * @param address
	 * @param txid
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @return Object
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/transfer", method = RequestMethod.GET)
	public PageModel<WithdrawDetailDto> transfer(@AuthForHeader AuthContext context,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "txid", required = false) String txid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "email", required = false)  String email,
			@RequestParam(value = "status", required = false) WithdrawCoinOrderStatus status,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryOrder(brokerId, id, account, uId,
				address, txid, assetCode,beginTime,endTime,email, status, pageNo, pageSize);
		PageModel<WithdrawDetailDto> pageModel = new PageModel<WithdrawDetailDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(
				pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList()));

		return pageModel;
	}
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/processed-query", method = RequestMethod.GET)
	public PageModel<WithdrawDetailDto> transferAlreadyProcessed(@AuthForHeader AuthContext context,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "txid", required = false) String txid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "email", required = false)  String email,
			@RequestParam(value = "status", required = false) WithdrawCoinOrderType status,
			@RequestParam(value = "sortProp", required = false) String sortProp,
			@RequestParam(value = "sortOrder", required = false) String sortOrder,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		String orderBy = null;
		if(sortProp == null || sortOrder == null){
			orderBy = "update_date desc";
		}else{
			orderBy = sortProp+" "+sortOrder;
		}
		PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryAlreadyProcessedOrder(brokerId, id, account, uId,
				address, txid, assetCode,beginTime,endTime,email, status,orderBy, pageNo, pageSize);
		PageModel<WithdrawDetailDto> pageModel = new PageModel<WithdrawDetailDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(
				pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList()));

		return pageModel;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/untreated", method = RequestMethod.GET)
	public PageModel<WithdrawDetailDto> transferUntreated(@AuthForHeader AuthContext context,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "txid", required = false) String txid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "email", required = false)  String email,
			@RequestParam(value = "status", required = false) WithdrawCoinOrderType status,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryAlreadyProcessedOrder(brokerId, id, account, uId,
				address, txid, assetCode,beginTime,endTime,email, status,"update_date desc", pageNo, pageSize);
		PageModel<WithdrawDetailDto> pageModel = new PageModel<WithdrawDetailDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(
				pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList()));

		return pageModel;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/inuntreated", method = RequestMethod.GET)
	public PageModel<WithdrawDetailDto> intransferUntreated(@AuthForHeader AuthContext context,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "txid", required = false) String txid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "email", required = false)  String email,
			@RequestParam(value = "status", required = false) WithdrawCoinOrderType status,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<WithdrawCoinOrderUser> pageInfo = withdrawCoinQueryService.queryInAlreadyProcessedOrder(brokerId, id, account, uId,
				address, txid, assetCode,beginTime,endTime,email, status,"update_date desc", pageNo, pageSize);
		PageModel<WithdrawDetailDto> pageModel = new PageModel<WithdrawDetailDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(
				pageInfo.getList().stream().map(order -> new WithdrawDetailDto(order)).collect(Collectors.toList()));

		return pageModel;
	}




	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@ResponseBody
	@RequestMapping(value = "/confirm", method = RequestMethod.GET)
	public void confirm(@AuthForHeader AuthContext context,
			@RequestParam(value = "refuseMs", required = false) String refuseMs,
			@RequestParam("id") Integer id,
			@RequestParam("confirm") Confirm confirm) {

		int adminId = context.getLoginSession().getUserId();
		WithdrawCoinOrderUser order = withdrawCoinQueryService.getOrderById(id);

		switch (confirm) {
		case ADOPT:
			withdrawCoinService.withdraw(id, adminId);
			break;
		case REFUSE:
			withdrawCoinService.withdrawRefund(id, adminId,refuseMs);
			break;
		default:
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
	}

	enum Confirm {
		ADOPT, REFUSE
	}

	@Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@ResponseBody
	@RequestMapping(value = "/checkaddress", method = RequestMethod.GET)
	public boolean confirm(@RequestParam(value = "address") String address) {
		ChannelCoinAddressDepositPool info = channelCoinAddressDepositPoolService.selectByCoinAddress(address);
		if(info == null) {
			return false;
		}else {
			return true;
		}
	}

}
