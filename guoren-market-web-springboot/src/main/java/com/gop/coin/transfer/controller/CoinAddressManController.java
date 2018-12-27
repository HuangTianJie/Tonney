package com.gop.coin.transfer.controller;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.dto.AddressDto;
import com.gop.coin.transfer.dto.TransferInAddressDto;
import com.gop.coin.transfer.dto.TransferOutDto;
import com.gop.coin.transfer.facade.CoinTransferFacade;
import com.gop.coin.transfer.service.ChannelCoinAddressDepositInfoService;
import com.gop.coin.transfer.service.DepositCoinAddressService;
import com.gop.coin.transfer.service.WithdrawCoinAddressService;
import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.ChannelCoinAddressDepositInfo;
import com.gop.domain.ChannelCoinAddressWithdraw;
import com.gop.domain.ConfigAsset;
import com.gop.domain.User;
import com.gop.domain.enums.CurrencyType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController
@RequestMapping("/coin")
public class CoinAddressManController {
	// 获取转出地址
	@Autowired
	private ConfigAssetService configAssetService;

	@Autowired
	private DepositCoinAddressService depositService;
	@Autowired
	private CoinTransferFacade coinTransferFacade;

	@Autowired
	private WithdrawCoinAddressService withdrawService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ChannelCoinAddressDepositInfoService channelCoinAddressDepositInfoService;

	//查询转出地址
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/transfer-out-address-query", method = RequestMethod.GET)
	public PageModel<AddressDto> getCoinTransferOutAddress(@AuthForHeader AuthContext context,
			@RequestParam("assetCode") String assetCode, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNo") Integer pageNo) {

		ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
		if (configAsset == null) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		int uid = context.getLoginSession().getUserId();
		PageInfo<ChannelCoinAddressWithdraw> pageInfo = withdrawService.getTransferList(uid, assetCode, pageNo,
				pageSize);

		PageModel<AddressDto> pageModel = new PageModel<>();
		pageModel.setPageSize(pageSize);
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());

		pageModel.setList(
				pageInfo.getList().stream().map(address -> new AddressDto(address)).collect(Collectors.toList()));
		return pageModel;
	}


	// 添加转出地址
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/transfer-out-address-add", method = RequestMethod.POST)
	public void addCoinTransferOutAddress(@AuthForHeader AuthContext context,
			@RequestBody TransferOutDto transferInDto) {
		ConfigAsset configAsset = configAssetService.getAssetConfig(transferInDto.getAssetCode());
		if (configAsset == null) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		int uid = context.getLoginSession().getUserId();
		
		
		withdrawService.addWithdrawCoinAddress(transferInDto.getAddress(), transferInDto.getMemo(), uid,
				transferInDto.getAssetCode());
	}

	// 删除转出地址
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/transfer-out-address-delete", method = RequestMethod.GET)
	public void deleteCoinTransferOutAddress(@AuthForHeader AuthContext context, @RequestParam("id") Integer id) {
		int uid = context.getLoginSession().getUserId();

		withdrawService.deleteWithdrawCoinAddress(id, uid);
	}

	// 获取转入地址
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/transfer-in-address", method = RequestMethod.GET)
	public List<TransferInAddressDto> getCoinTransferInAddress(@AuthForHeader AuthContext context,
			@RequestParam("assetCode") String assetCode) {
		if (null != assetCode) {
			ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
			if (configAsset == null) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		int uid = context.getLoginSession().getUserId();
		//校验用户是否设置资金密码
		//TODO
//		User user = userService.getUserByUid(uid);
//		if (user == null) {
//			context.setLoginSession(null);
//			throw new AppException(SecurityCodeConst.NO_LOGIN);
//		}
//		if (Strings.isNullOrEmpty(user.getPayPassword())) {
//			throw new AppException(SecurityCodeConst.NO_SET_PAY_PASSWORD);
//		}
		List<ChannelCoinAddressDeposit> lst = null;
		ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
		if (null == configAsset) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		lst = depositService.getCoinDepositAddressList(assetCode, uid);
		if (lst == null || lst.isEmpty()) {
			coinTransferFacade.createCoinAddress(assetCode, uid, assetCode);
			lst = depositService.getCoinDepositAddressList(assetCode, uid);
		}
		if (lst == null) {
			lst = Collections.EMPTY_LIST;
		}
		return lst.stream().map(address -> new TransferInAddressDto(address)).collect(Collectors.toList());
	}

}
