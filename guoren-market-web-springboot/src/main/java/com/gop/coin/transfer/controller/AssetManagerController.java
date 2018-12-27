package com.gop.coin.transfer.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.code.consts.WithdrawalsCodeConst;
import com.gop.coin.transfer.dto.DepositCoinDto;
import com.gop.coin.transfer.dto.WithDrawCoinDto;
import com.gop.coin.transfer.dto.WithdrawCoinDetailDto;
import com.gop.coin.transfer.dto.WithdrawCoinFeeQueryDto;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinAddressService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinService;
import com.gop.config.CommonConfig;
import com.gop.domain.ChannelCoinAddressWithdraw;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.DepositCoinOrderUser;
import com.gop.domain.User;
import com.gop.domain.UserGoogleCodeConfig;
import com.gop.domain.WithdrawCoinOrderUser;
import com.gop.domain.enums.AssetStatus;
import com.gop.domain.enums.AuthLevel;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gop.domain.enums.DelFlag;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserGoogleCodeConfigService;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/coin")
@Slf4j
public class AssetManagerController {

	private static final int ADDRESS_LENGTH_LIMIT = 70;
	@Autowired
	private ConfigAssetService configAssetService;

	@Autowired
	private WithdrawCoinQueryService coinWithdrawService;

	@Autowired
	private WithdrawCoinService withdrawCoinService;

	@Autowired
	private WithdrawCoinAddressService withdrawCoinAddressService;

	@Autowired
	private ConfigAssetProfileService configAssetProfileService;

	@Autowired
	private UserGoogleCodeConfigService userGoogleCodeConfigService;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	private DepositCoinQueryService depositCoinQueryService;

	@Autowired
	private StringRedisTemplate redisTemplate; 

	@Autowired
	@Qualifier("userService")
	private UserService userService;

	@Autowired
	private Gson gson; 

	// 查询果仁转出历史
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/withdraw-coin-details", method = RequestMethod.GET)
	public PageModel<WithdrawCoinDetailDto> getWithdrawCoin(@AuthForHeader AuthContext context,
			@RequestParam("assetCode") String assetCode,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		if (null != assetCode) {
			ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
//			if (configAsset == null || !configAsset.getCurrencyType().equals(CurrencyType.COIN)) {
			if (configAsset == null){
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		int uid = context.getLoginSession().getUserId();

		if (pageNo == null || pageSize == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		PageInfo<WithdrawCoinOrderUser> pageInfos = coinWithdrawService.getTransferList(uid, assetCode, pageSize,
				pageNo);
		PageModel<WithdrawCoinDetailDto> pages = new PageModel<>();
		pages.setPageNo(pageNo);
		pages.setPageNum(pageInfos.getPages());
		pages.setPageSize(pageSize);
		pages.setList(pageInfos.getList().stream().map(order -> new WithdrawCoinDetailDto(order))
				.collect(Collectors.toList()));
		return pages;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkGoogleCodeStrategy'}})") })
	@RequestMapping(value = "/withdraw-coin", method = RequestMethod.POST)
	public void WithdrawCoin(@AuthForHeader AuthContext context, @Valid @RequestBody WithDrawCoinDto dto) {
		int uid = context.getLoginSession().getUserId();
		// 重置谷歌验证码后24小时内不允许提币
		UserGoogleCodeConfig userGoogleCodeConfig = userGoogleCodeConfigService.selectUserGoogleCodeConfigByUid(uid);
		if (null != userGoogleCodeConfig && null != userGoogleCodeConfig.getResetDate()) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(userGoogleCodeConfig.getResetDate());
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			long resetTime = calendar.getTime().getTime();
			if (resetTime > new Date().getTime()) {
				throw new AppException(WithdrawalsCodeConst.RESET_GOOGLE_CODE_WITHDRAW_LIMIT);
			}
		}

		ConfigAsset configAsset = configAssetService.getAssetConfig(dto.getAssetCode());
		if (configAsset == null) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}

		if (Strings.isEmpty(dto.getAddress())) {

			if (null == dto.getAddressId()) {
				throw new AppException(CommonCodeConst.FIELD_ERROR);
			}

			ChannelCoinAddressWithdraw address = withdrawCoinAddressService.getById(dto.getAddressId());

			if (null == address || address.getDelFlag().equals(DelFlag.TRUE) || uid != address.getUid().intValue()) {
				log.info("获取货币转出地址失败,assetCode:{},addressId:{}", dto.getAssetCode(), dto.getAddressId());
				throw new AppException(CommonCodeConst.FIELD_ERROR, "地址状态异常");
			}
			dto.setAddress(address.getCoinAddress());
		}
		// 校验输入地址(目前不能大于70位)
		if (dto.getAddress().length() > ADDRESS_LENGTH_LIMIT) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "地址状态异常");
		}

		log.info("开始传出货币");

		withdrawCoinService.withdrawCoinOrder(uid, dto.getOutOrder(), dto.getAssetCode(), dto.getAmount(), dto.getFee(),
				dto.getAddress(), dto.getMessage());

		log.info("转出成功");
	}

	//手机号登录转出
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})") })
	@RequestMapping(value = "/phone-withdraw-coin", method = RequestMethod.POST)
	public void PhoneWithdrawCoin(@AuthForHeader AuthContext context, @Valid @RequestBody WithDrawCoinDto dto) {
		//		int uid = context.getLoginSession().getUserId();
		//		log.info("手机号登录转出uid:{}",uid);
		String mobile = dto.getMobileid();
		User info = userService.getUserLikePhone(mobile);
		String code = dto.getCode();
		int uid = info.getUid();
		log.info("手机号登录转出:{}",uid);
		//校验验证码
		String rediscode = redisTemplate.opsForValue().get("rollout"+mobile+":code");
		log.info("手机号登录转出1:{}",code.equals(rediscode));
		if(!code.equals(rediscode)){
			throw new AppException(CommonCodeConst.CODE_ERROR,"验证码输入错误！");
		}
		ConfigAsset configAsset = configAssetService.getAssetConfig(dto.getAssetCode());
		if (configAsset == null) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		log.info("手机号登录转出2:{}",dto.getAddress());
		if (Strings.isEmpty(dto.getAddress())) {

			if (null == dto.getAddressId()) {
				throw new AppException(CommonCodeConst.FIELD_ERROR);
			}

			ChannelCoinAddressWithdraw address = withdrawCoinAddressService.getById(dto.getAddressId());
			log.info("手机号登录转出3:{}",address.getCoinAddress());
			if (null == address || address.getDelFlag().equals(DelFlag.TRUE) || uid != address.getUid().intValue()) {
				log.info("获取货币转出地址失败,assetCode:{},addressId:{}", dto.getAssetCode(), dto.getAddressId());
				throw new AppException(CommonCodeConst.FIELD_ERROR, "地址状态异常");
			}
			dto.setAddress(address.getCoinAddress());
		}
		// 校验输入地址(目前不能大于70位)
		if (dto.getAddress().length() > ADDRESS_LENGTH_LIMIT) {
			throw new AppException(CommonCodeConst.FIELD_ERROR, "地址状态异常");
		}

		log.info("开始传出货币");

		withdrawCoinService.withdrawCoinOrder(uid, dto.getOutOrder(), dto.getAssetCode(), dto.getAmount(), dto.getFee(),
				dto.getAddress(), dto.getMessage());

		redisTemplate.delete("rollout"+mobile+":code");
		redisTemplate.delete("rollout"+mobile+":avoid");
		log.info("转出成功");
	}

	// 用户提币时,对应币种手续费查询
	// 可以非登录用户查询,供展示用
	// @Strategys(strategys = @Strategy(authStrategy =
	// "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/withdraw-coinfee-query", method = RequestMethod.GET)
	public WithdrawCoinFeeQueryDto withdrawCoinfeeQuery(@AuthForHeader AuthContext context,
			@RequestParam("key") ConfigAssetType key, @RequestParam(value = "assetCode") String assetCode) {
		// 校验币种是否有效
		boolean validatedAsset = configAssetService.validateAssetConfig(assetCode, AssetStatus.LISTED);
		if (!validatedAsset) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}
		String value = configAssetProfileService.getStringValue(assetCode, key);
		WithdrawCoinFeeQueryDto dto = new WithdrawCoinFeeQueryDto();
		dto.setAssetCode(assetCode);
		dto.setProfileKey(key);
		dto.setProfileValue(value);
		return dto;
	}
	//查询用户对应的可提取币种
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/withdraw-coin-asset-list", method = RequestMethod.GET)
	public List<String> getWithdrawCoinAssetList(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		ConfigAssetType key = getConfigAssetType(user.getAuthLevel());
		List<ConfigAssetProfile> list = configAssetProfileService.getConfigAssetProfileList(key);
		List<String> availableList = list.stream()
				.filter(profile -> Double.valueOf(profile.getProfileValue()).compareTo(0.0) >= 0)
				.map(profile -> profile.getAssetCode()).collect(Collectors.toList());
		return availableList;
	}

	public ConfigAssetType getConfigAssetType(AuthLevel value) {
		switch (value) {
		case LEVEL0:
			return ConfigAssetType.WITHDRAWLEVEL_0;
		case LEVEL1:
			return ConfigAssetType.WITHDRAWLEVEL_1;
		case LEVEL2:
			return ConfigAssetType.WITHDRAWLEVEL_2;
		default:
			return null;
		}
	}
	//所有币种的配置列表
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/assetprofile-list", method = RequestMethod.GET)
	public List<ConfigAssetProfile> getConfigAssetProfileList(@AuthForHeader AuthContext context,
			@RequestParam(value = "assetCode", required = false) String assetCode,
			@RequestParam(value = "key", required = false) ConfigAssetType key) {
		List<ConfigAssetProfile> list = configAssetProfileService.selectByAssetCodeOrProfileKey(assetCode, key);
		return list;
	}


	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/deposit-coin-details", method = RequestMethod.GET)
	public PageModel<DepositCoinDto> getDepositCoin(@AuthForHeader AuthContext context,
			@RequestParam("assetCode") String assetCode,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		if (null != assetCode) {
			ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
//			if (configAsset == null || !configAsset.getCurrencyType().equals(CurrencyType.COIN)) {
			if (configAsset == null) {
				throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
			}
		}
		int uid = context.getLoginSession().getUserId();

		if (pageNo == null || pageSize == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		PageInfo<DepositCoinOrderUser> pageInfos = depositCoinQueryService.getTransferList(uid, assetCode, pageSize,
				pageNo);
		PageModel<DepositCoinDto> pages = new PageModel<>();
		pages.setPageNo(pageNo);
		pages.setPageNum(pageInfos.getPages());
		pages.setPageSize(pageSize);
		pages.setList(
				pageInfos.getList().stream().map(order -> new DepositCoinDto(order)).collect(Collectors.toList()));
		return pages;
	}

	// 所有币种列表
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/configasset-list", method = RequestMethod.GET)
	public List<ConfigAsset> getConfigAssetList(@AuthForHeader AuthContext context,
			@RequestParam(value = "status", required = false) AssetStatus status) {
		List<ConfigAsset> list = configAssetService.getAllAssetCode();
		return list;
	}
	//
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/deposit-coin-asset-list", method = RequestMethod.GET)
	public List<String> getDepositCoinAssetList(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		ConfigAssetType key = getDepositTpye(user.getAuthLevel());
		List<ConfigAssetProfile> list = configAssetProfileService.getConfigAssetProfileList(key);
		List<String> availableList = list.stream()
				.filter(profile -> Double.valueOf(profile.getProfileValue()).compareTo(0.0) >= 0)
				.map(profile -> profile.getAssetCode()).collect(Collectors.toList());
		return availableList;
	}


	public ConfigAssetType getDepositTpye(AuthLevel value) {
		switch (value) {
		case LEVEL0:
			return ConfigAssetType.DEPOSITLEVEL_DEFAULT;
		case LEVEL1:
			return ConfigAssetType.DEPOSITLEVEL_DEFAULT;
		case LEVEL2:
			return ConfigAssetType.DEPOSITLEVEL_DEFAULT;
		default:
			return null;
		}
	}

	//查询用户对应的可提取币种
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/app-withdraw-coin-asset-list", method = RequestMethod.GET)
	public Map<String,Object> getAppWithdrawCoinAssetList(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		Map<String,Object> map= new HashMap<String, Object>();
		ConfigAssetType key = getConfigAssetType(user.getAuthLevel());
		List<ConfigAssetProfile> list = configAssetProfileService.getConfigAssetProfileList(key);
		List<String> availableList = list.stream()
				.filter(profile -> Double.valueOf(profile.getProfileValue()).compareTo(0.0) >= 0)
				.map(profile -> profile.getAssetCode()).collect(Collectors.toList());
		List<String> coinList= new ArrayList<String>();
		List<String> tokenList= new ArrayList<String>();
		List<String> cashList= new ArrayList<String>();
		for(int i = 0;i<availableList.size();i++) {
			String code = availableList.get(i);
			ConfigAsset info = configAssetService.getAssetConfig(code);
			if("COIN".equals(info.getCurrencyType().toString())) {
				coinList.add(code);
			}else if("TOKEN".equals(info.getCurrencyType().toString())){
				tokenList.add(code);
			}else if("CASH".equals(info.getCurrencyType().toString())){
				cashList.add(code);
			}
		}
		map.put("coinList", coinList);
		map.put("tokenList", tokenList);
		map.put("cashList", cashList);
		return map;
	}
	
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/app-deposit-coin-asset-list", method = RequestMethod.GET)
	public Map<String,Object> getAppDepositCoinAssetList(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		Map<String,Object> map= new HashMap<String, Object>();
		User user = userFacade.getUser(uid);
		ConfigAssetType key = getDepositTpye(user.getAuthLevel());
		List<ConfigAssetProfile> list = configAssetProfileService.getConfigAssetProfileList(key);
		List<String> availableList = list.stream()
				.filter(profile -> Double.valueOf(profile.getProfileValue()).compareTo(0.0) >= 0)
				.map(profile -> profile.getAssetCode()).collect(Collectors.toList());
		List<String> coinList= new ArrayList<String>();
		List<String> tokenList= new ArrayList<String>();
		List<String> cashList= new ArrayList<String>();
		for(int i = 0;i<availableList.size();i++) {
			String code = availableList.get(i);
			ConfigAsset info = configAssetService.getAssetConfig(code);
			if("COIN".equals(info.getCurrencyType().toString())) {
				coinList.add(code);
			}else if("TOKEN".equals(info.getCurrencyType().toString())){
				tokenList.add(code);
			}else if("CASH".equals(info.getCurrencyType().toString())){
				cashList.add(code);
			}
		}
		map.put("coinList", coinList);
		map.put("tokenList", tokenList);
		map.put("cashList", cashList);
		return map;
	}
}
