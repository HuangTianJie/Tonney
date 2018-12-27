package com.gop.c2c.normaltransaction;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.gop.c2c.dto.C2cAdvertBuySimpleDto;
import com.gop.c2c.dto.C2cBuyAdvertDeployDto;
import com.gop.c2c.dto.C2cBuyAdvertEditDto;
import com.gop.c2c.dto.C2cBuyAdvertInitDto;
import com.gop.c2c.service.C2cAlipayInfoService;
import com.gop.c2c.service.C2cBankInfoService;
import com.gop.c2c.service.C2cBuyAdvertisementService;
import com.gop.c2c.service.C2cGetAdvertisementAssetCodeConfigService;
import com.gop.c2c.service.C2cGetAdvertisementCountryConfigService;
import com.gop.c2c.service.C2cGetAdvertisementCurrencyConfigService;
import com.gop.c2c.service.C2cUserTransactionInfoService;
import com.gop.code.consts.C2cCodeConst;
import com.gop.domain.C2cBuyAdvertisement;
import com.gop.domain.enums.C2cBuyAdvertStatus;
import com.gop.domain.enums.C2cTransType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.util.BigDecimalUtils;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求购广告
 * @author sunhaotian
 */
@RestController("UserAdvertBuyController")
@RequestMapping("/c2cbuyadvert")
@Slf4j
public class UserAdvertBuyController {


	@Autowired
	private UserFacade userFacade;

	@Autowired
	private C2cBuyAdvertisementService c2cBuyAdvertisementService;

	@Autowired
	private C2cGetAdvertisementAssetCodeConfigService c2cAssetCodeConfigService;

	@Autowired
	private C2cGetAdvertisementCountryConfigService c2cCountryConfigService;

	@Autowired
	private C2cGetAdvertisementCurrencyConfigService c2cCurrencyConfigService;

	@Autowired
	private C2cUserTransactionInfoService c2cUserTransactionInfoService;

	@Autowired
	private C2cBankInfoService c2cBankInfoService;

	@Autowired
	private C2cAlipayInfoService c2cAlipayInfoService;


	//发布求购广告
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/advert-deploy", method = RequestMethod.POST)
	public void advertDeploy(@AuthForHeader AuthContext context,@RequestBody C2cBuyAdvertDeployDto c2cBuyAdvertDeployDto) {
		//校验是否设置过昵称
		Integer uid = context.getLoginSession().getUserId();
		String nickname =userFacade.getUser(uid).getNickname();
		if(Strings.isNullOrEmpty(nickname)) {
			throw new AppException(C2cCodeConst.NO_SET_NICKNAME);
		}
		//零值校验
		if(BigDecimalUtils.isLessZero(c2cBuyAdvertDeployDto.getTradePrice())) {
			throw new AppException(C2cCodeConst.ADVERT_TRADE_PRICE_LESS_ZERO);
		}
		if(BigDecimalUtils.isLessZero(c2cBuyAdvertDeployDto.getBuyPrice())) {
			throw new AppException(C2cCodeConst.ADVERT_BUY_PRICE_LESS_ZERO);
		}
		//校验交易币种类型
		if (!c2cAssetCodeConfigService.checkStatusListedAssetCodeByType(c2cBuyAdvertDeployDto.getAssetcode(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_ASSET_CODE_INVALID);
		}
		//校验国家类型
		if (!c2cCountryConfigService.checkStatusListedCountryByType(c2cBuyAdvertDeployDto.getCountry(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_COUNTRY_INVALID);
		}
		//校验货币类型
		if (!c2cCurrencyConfigService.checkStatusListedCurrencyByType(c2cBuyAdvertDeployDto.getCurrency(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_CURRENCY_INVALID);
		}

		C2cBuyAdvertisement c2cBuyAdvertisement = new C2cBuyAdvertisement();
		c2cBuyAdvertisement.setUid(uid);
		c2cBuyAdvertisement.setNickname(nickname);
		c2cBuyAdvertisement.setAssetCode(c2cBuyAdvertDeployDto.getAssetcode());
		c2cBuyAdvertisement.setCountry(c2cBuyAdvertDeployDto.getCountry());
		c2cBuyAdvertisement.setCurrency(c2cBuyAdvertDeployDto.getCurrency());
		c2cBuyAdvertisement.setTradePrice(c2cBuyAdvertDeployDto.getTradePrice());
		c2cBuyAdvertisement.setBuyPrice(c2cBuyAdvertDeployDto.getBuyPrice());
		c2cBuyAdvertisement.setRemark(c2cBuyAdvertDeployDto.getRemark());
		c2cBuyAdvertisement.setStatus(C2cBuyAdvertStatus.SHOW);
		c2cBuyAdvertisementService.addC2cBuyAdvertisement(c2cBuyAdvertisement);
	}

	//发布求购广告页面初始化信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/get-info", method = RequestMethod.GET)
	public C2cBuyAdvertInitDto getInfo(@AuthForHeader AuthContext context) {
		//将交易币种、国家、货币列表、付款方式存入dto
		C2cBuyAdvertInitDto c2cBuyAdvertInitDto = C2cBuyAdvertInitDto.builder()
			.assetcodeList(c2cAssetCodeConfigService.getC2cAssetCodeConfig(C2cTransType.BUY))
			.countryList(c2cCountryConfigService.getC2cCountryConfig(C2cTransType.BUY))
			.currencyList(c2cCurrencyConfigService.getC2cCurrencyConfig(C2cTransType.BUY))
			.build();
		return c2cBuyAdvertInitDto;
	}

	//查询求购展示信息
	@RequestMapping(value = "/advert-query", method = RequestMethod.GET)
	public PageModel<C2cAdvertBuySimpleDto> advertQuery(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
		List<C2cBuyAdvertisement> c2cBuyAdverts = c2cBuyAdvertisementService.selectAllByShowStatus(pageNo,pageSize);

		if (null == c2cBuyAdverts || c2cBuyAdverts.isEmpty()) {
			return new PageModel<>();
		}
		List<C2cAdvertBuySimpleDto> advertBuySimpleDtos = Lists.newArrayList();
		for(C2cBuyAdvertisement c2cBuyAdvert : c2cBuyAdverts) {
			C2cAdvertBuySimpleDto advertBuySimpleDto = C2cAdvertBuySimpleDto.builder()
					.advertId(c2cBuyAdvert.getAdvertId())
					.nickName(c2cBuyAdvert.getNickname())
					.assetCode(c2cBuyAdvert.getAssetCode())
					.country(c2cBuyAdvert.getCountry())
					.currency(c2cBuyAdvert.getCurrency())
					.tradePrice(c2cBuyAdvert.getTradePrice())
					.buyPrice(c2cBuyAdvert.getBuyPrice())
					.createDate(c2cBuyAdvert.getCreateDate())
					.remark(c2cBuyAdvert.getRemark())
					.c2cTransactionInfoDto(c2cUserTransactionInfoService.getUserTransactionInfo(c2cBuyAdvert.getUid()))
					.build();
			advertBuySimpleDtos.add(advertBuySimpleDto);
		}
		PageInfo<C2cBuyAdvertisement> pageInfo = new PageInfo<>(c2cBuyAdverts);
		PageModel<C2cAdvertBuySimpleDto> pageModel = new PageModel<C2cAdvertBuySimpleDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(advertBuySimpleDtos);
		return pageModel;
	}

	//查询求购详情信息
	@RequestMapping(value = "/advert-detail", method = RequestMethod.GET)
	public C2cBuyAdvertisement advertDetail(@RequestParam("advertId") String advertId) {
		C2cBuyAdvertisement c2cBuyAdvert = c2cBuyAdvertisementService.selectAllStatusByAdvertId(advertId);
		if (c2cBuyAdvert == null) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_EXIST);
		}
		if(!C2cBuyAdvertStatus.SHOW.equals(c2cBuyAdvert.getStatus())) {
			throw new AppException(C2cCodeConst.ADVERT_HAS_DELETED);
		}
		return c2cBuyAdvert;
	}

	//求购广告编辑页面展示
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/advert-edit", method = RequestMethod.GET)
	public C2cBuyAdvertEditDto advertEdit(@AuthForHeader AuthContext context,@RequestParam("advertId") String advertId) {
		C2cBuyAdvertisement c2cBuyAdvert = c2cBuyAdvertisementService.selectAllStatusByAdvertId(advertId);
		if (c2cBuyAdvert == null) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_EXIST);
		}
		Integer uid = context.getLoginSession().getUserId();
		//校验广告是否属于该用户
		if(!Objects.equals(uid, c2cBuyAdvert.getUid())) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_BELONG_TO_USER);
		}
		//校验广告是否已删除
		if(C2cBuyAdvertStatus.DELETED.equals(c2cBuyAdvert.getStatus())) {
			//该广告已过期
			throw new AppException(C2cCodeConst.ADVERT_HAS_OVERDUE);
		}
		C2cBuyAdvertEditDto c2cBuyAdvertEditDto = C2cBuyAdvertEditDto.builder()
				.assetcodeList(c2cAssetCodeConfigService.getC2cAssetCodeConfig(C2cTransType.BUY))
				.countryList(c2cCountryConfigService.getC2cCountryConfig(C2cTransType.BUY))
				.currencyList(c2cCurrencyConfigService.getC2cCurrencyConfig(C2cTransType.BUY))
				.assetcode(c2cBuyAdvert.getAssetCode())
				.country(c2cBuyAdvert.getCountry())
				.currency(c2cBuyAdvert.getCurrency())
				.tradePrice(c2cBuyAdvert.getTradePrice())
				.buyPrice(c2cBuyAdvert.getBuyPrice())
				.remark(c2cBuyAdvert.getRemark()).build();
		return c2cBuyAdvertEditDto;
	}

	//求购广告保存编辑
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/ensure-edit", method = RequestMethod.POST)
	public void ensureEdit(@AuthForHeader AuthContext context,@RequestBody C2cBuyAdvertDeployDto c2cBuyAdvertDeployDto) {
		//零值校验
		if(BigDecimalUtils.isLessZero(c2cBuyAdvertDeployDto.getTradePrice())) {
			throw new AppException(C2cCodeConst.ADVERT_TRADE_PRICE_LESS_ZERO);
		}
		if(BigDecimalUtils.isLessZero(c2cBuyAdvertDeployDto.getBuyPrice())) {
			throw new AppException(C2cCodeConst.ADVERT_BUY_PRICE_LESS_ZERO);
		}
		//校验交易币种类型
		if (!c2cAssetCodeConfigService.checkStatusListedAssetCodeByType(c2cBuyAdvertDeployDto.getAssetcode(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_ASSET_CODE_INVALID);
		}
		//校验国家类型
		if (!c2cCountryConfigService.checkStatusListedCountryByType(c2cBuyAdvertDeployDto.getCountry(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_COUNTRY_INVALID);
		}
		//校验货币类型
		if (!c2cCurrencyConfigService.checkStatusListedCurrencyByType(c2cBuyAdvertDeployDto.getCurrency(), C2cTransType.BUY)) {
			throw new AppException(C2cCodeConst.ADVERT_CURRENCY_INVALID);
		}
		Integer uid = context.getLoginSession().getUserId();
		String nickname =userFacade.getUser(uid).getNickname();
		C2cBuyAdvertisement c2cBuyAdvertisement = new C2cBuyAdvertisement();
		c2cBuyAdvertisement.setUid(uid);
		c2cBuyAdvertisement.setNickname(nickname);
		c2cBuyAdvertisement.setAdvertId(c2cBuyAdvertDeployDto.getAdvertId());
		c2cBuyAdvertisement.setAssetCode(c2cBuyAdvertDeployDto.getAssetcode());
		c2cBuyAdvertisement.setCountry(c2cBuyAdvertDeployDto.getCountry());
		c2cBuyAdvertisement.setCurrency(c2cBuyAdvertDeployDto.getCurrency());
		c2cBuyAdvertisement.setTradePrice(c2cBuyAdvertDeployDto.getTradePrice());
		c2cBuyAdvertisement.setBuyPrice(c2cBuyAdvertDeployDto.getBuyPrice());
		c2cBuyAdvertisement.setRemark(c2cBuyAdvertDeployDto.getRemark());
		c2cBuyAdvertisement.setStatus(C2cBuyAdvertStatus.SHOW);
		c2cBuyAdvertisementService.ensureEditBuyAdvertisement(c2cBuyAdvertisement);

	}

	//求购广告删除
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/advert-remove", method = RequestMethod.GET)
	public void advertRemove(@AuthForHeader AuthContext context,@RequestParam("advertId") String advertId) {
		c2cBuyAdvertisementService.delBuyAdvertisementByAdvertId(advertId);
	}

	//查询我的求购广告信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/myadvert-query", method = RequestMethod.GET)
	public PageModel<C2cBuyAdvertisement> myAdvertQuery(@AuthForHeader AuthContext context,@RequestParam("pageNo") Integer pageNo,
			@RequestParam("pageSize") Integer pageSize) {
		Integer uid = context.getLoginSession().getUserId();
		List<C2cBuyAdvertisement> c2cBuyAdverts = c2cBuyAdvertisementService.selectC2cBuyAdvertByUid(uid,pageNo,pageSize);
		if (null == c2cBuyAdverts || c2cBuyAdverts.isEmpty()) {
			 return new PageModel<>();
		}
		PageInfo<C2cBuyAdvertisement> pageInfo = new PageInfo<>(c2cBuyAdverts);
		PageModel<C2cBuyAdvertisement> pageModel = new PageModel<C2cBuyAdvertisement>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(c2cBuyAdverts);
		return pageModel;
	}

}
