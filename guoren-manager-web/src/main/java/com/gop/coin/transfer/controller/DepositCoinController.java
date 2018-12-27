package com.gop.coin.transfer.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.gop.coin.transfer.dto.DepositDetailDto;
import com.gop.coin.transfer.service.DepositCoinOrderService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.domain.DepositCoinOrderUser;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.mode.vo.PageModel;
import com.gop.util.SequenceUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("managerDepositCoinController")
@RequestMapping("/deposit/coin")
public class DepositCoinController {

	@Autowired
	private DepositCoinQueryService depositCoinQueryService;

	@Autowired
	private DepositCoinOrderService depositCoinOrderServiceImpl;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/uncerf", method = RequestMethod.GET)
	public void depositCoin(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("assetCode") String assetCode, @RequestParam("amount") BigDecimal amount) {

		String txid = SequenceUtil.getNextId();

		depositCoinOrderServiceImpl.coinDepositOrderUnCerf(uid, txid, assetCode, amount);
		;

	}

	/**
	 * * 转果仁记录查询
	 * 
	 * @param optType-转入、转出
	 * @param phone
	 * @param id
	 * @param uId
	 * @param address
	 * @param txid
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @param request
	 * @return Object
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/transfer", method = RequestMethod.GET)
	// @ApiOperation("转果仁记录查询")
	public PageModel<DepositDetailDto> transfer(@AuthForHeader AuthContext context,
			@RequestParam(value = "brokerId", required = false) Integer brokerId,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "accout", required = false) String account,
			@RequestParam(value = "uId", required = false) Integer uId,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "txid", required = false) String txid,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "beginTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
			@RequestParam(value = "email", required = false)  String email,
			@RequestParam(value = "status", required = false) DepositCoinAssetStatus status,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		PageInfo<DepositCoinOrderUser> pageInfo = depositCoinQueryService.queryOrder(brokerId, id, account, uId,
				address, txid, assetCode,beginTime,endTime,email, status,pageNo,pageSize);
		PageModel<DepositDetailDto> pageModel = new PageModel<DepositDetailDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(
				pageInfo.getList().stream().map(order -> new DepositDetailDto(order)).collect(Collectors.toList()));

		return pageModel;
	}
	


}
