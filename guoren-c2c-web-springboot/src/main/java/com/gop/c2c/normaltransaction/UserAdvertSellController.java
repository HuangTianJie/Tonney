package com.gop.c2c.normaltransaction;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.gop.asset.service.FinanceService;
import com.gop.c2c.dto.C2cAdvertSellBasicInfoDto;
import com.gop.c2c.dto.C2cAdvertSellDeployInfoDto;
import com.gop.c2c.dto.C2cAdvertSellDto;
import com.gop.c2c.dto.C2cAdvertSellSimpleDto;
import com.gop.c2c.dto.C2cAlipayInfoDto;
import com.gop.c2c.dto.C2cBankInfoDto;
import com.gop.c2c.dto.C2cBasePayChannelDto;
import com.gop.c2c.dto.C2cSellAdvertEditDto;
import com.gop.c2c.dto.C2cWechatpayInfoDto;
import com.gop.c2c.service.C2cAlipayInfoService;
import com.gop.c2c.service.C2cBankInfoService;
import com.gop.c2c.service.C2cGetAdvertisementAssetCodeConfigService;
import com.gop.c2c.service.C2cGetAdvertisementCountryConfigService;
import com.gop.c2c.service.C2cGetAdvertisementCurrencyConfigService;
import com.gop.c2c.service.C2cOrderPaymentDetailService;
import com.gop.c2c.service.C2cSellAdvertisementService;
import com.gop.c2c.service.C2cUserTransactionInfoService;
import com.gop.c2c.service.C2cWechatService;
import com.gop.code.consts.C2cCodeConst;
import com.gop.domain.C2cAlipayInfo;
import com.gop.domain.C2cBankInfo;
import com.gop.domain.C2cOrderPaymentDetail;
import com.gop.domain.C2cSellAdvertisement;
import com.gop.domain.C2cWeChatInfo;
import com.gop.domain.Finance;
import com.gop.domain.User;
import com.gop.domain.enums.C2cPayType;
import com.gop.domain.enums.C2cSellAdvertStatus;
import com.gop.domain.enums.C2cTransType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.util.BigDecimalUtils;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 售出广告
 * @author sunhaotian
 */
@RestController("UserAdvertSellController")
@RequestMapping("/c2cselladvert")
@Slf4j
public class UserAdvertSellController {

	@Autowired
	private C2cSellAdvertisementService c2cSellAdvertisementService;

	@Autowired
	private C2cGetAdvertisementAssetCodeConfigService c2cAssetCodeConfigService;

	@Autowired
	private C2cGetAdvertisementCountryConfigService c2cCountryConfigService;

	@Autowired
	private C2cGetAdvertisementCurrencyConfigService c2cCurrencyConfigService;

	@Autowired
	private C2cBankInfoService c2cBankInfoService;

	@Autowired
	private C2cAlipayInfoService c2cAlipayInfoService;

	@Autowired
	private C2cUserTransactionInfoService c2cUserTransactionInfoService;


	@Autowired
	private C2cOrderPaymentDetailService c2cOrderPaymentDetailService;

	@Autowired
	private FinanceService financeService;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	private C2cWechatService c2cWechatService;


	//售出信息发布
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	@RequestMapping(value = "/info-deploy", method = RequestMethod.POST)
	public void infoDeploy(@AuthForHeader AuthContext context,@RequestBody C2cAdvertSellDeployInfoDto advertSellSimpleDto) {
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		//校验是否设置过昵称
		if(Strings.isNullOrEmpty(user.getNickname())) {
			throw new AppException(C2cCodeConst.NO_SET_NICKNAME);
		}
		//校验是否设置过手机号
		if(Strings.isNullOrEmpty(user.getMobile())) {
			throw new AppException(C2cCodeConst.NO_SET_PHONE);
		}
		//校验交易币种类型
		if (!c2cAssetCodeConfigService.checkStatusListedAssetCodeByType(advertSellSimpleDto.getAssetcode(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_ASSET_CODE_INVALID);
		}
		//校验国家类型
		if (!c2cCountryConfigService.checkStatusListedCountryByType(advertSellSimpleDto.getCountry(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_COUNTRY_INVALID);
		}
		//校验货币类型
		if (!c2cCurrencyConfigService.checkStatusListedCurrencyByType(advertSellSimpleDto.getCurrency(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_CURRENCY_INVALID);
		}
		//交易价格零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getTradePrice())) {
			throw new AppException(C2cCodeConst.ADVERT_TRADE_PRICE_LESS_ZERO);
		}
		//最小售出个数零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getMinAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MIN_AMOUNT_LESS_ZERO);
		}
		//最大售出个数零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getMaxAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_LESS_ZERO);
		}
		//校验最大售出个数是否小于最小售出个数
		if(BigDecimalUtils.isBigger(advertSellSimpleDto.getMinAmount(), advertSellSimpleDto.getMaxAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_IS_LESS_MIN_AMOUNT);
		}
		Finance finance = financeService.queryAccount(uid, advertSellSimpleDto.getAssetcode());
		//空指针异常校验
		if(finance == null) {
			throw new AppException(C2cCodeConst.QUERY_USER_FINANCE_EXCEPTION);
		}
		//校验最大售出个数是否超过资产余额
		if(BigDecimalUtils.isBigger(advertSellSimpleDto.getMaxAmount(), finance.getAmountAvailable())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_IS_BIGGER_FINANCE);
		}
		//校验是否绑定了对应的支付通道
		for(C2cPayType c2cPayType : advertSellSimpleDto.getPayType()) {
			if("ALIPAY".equals(c2cPayType.name())) {
				C2cAlipayInfo  c2cAlipayInfo = c2cAlipayInfoService.selectByUid(uid);
				if (c2cAlipayInfo == null) {
					throw new AppException(C2cCodeConst.NO_SET_ALIPAY_INFO);
				}
			}else if("BANK".equals(c2cPayType.name())) {
				C2cBankInfo c2cBankInfo = c2cBankInfoService.selectByUid(uid);
				if (c2cBankInfo == null) {
					throw new AppException(C2cCodeConst.NO_SET_BANK_INFO);
				}
			}else if("WECHAT".equals(c2cPayType.name())) {
				C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(uid);
				if (info == null) {
					throw new AppException(C2cCodeConst.NO_SET_WECHAT_INFO);
				}
			}

		}
		C2cSellAdvertisement c2cSellAdvertisement = new C2cSellAdvertisement();
		c2cSellAdvertisement.setUid(uid);
		c2cSellAdvertisement.setNickname(user.getNickname());
		c2cSellAdvertisement.setAssetCode(advertSellSimpleDto.getAssetcode());
		c2cSellAdvertisement.setCountry(advertSellSimpleDto.getCountry());
		c2cSellAdvertisement.setPhone(user.getMobile());
		c2cSellAdvertisement.setCurrency(advertSellSimpleDto.getCurrency());
		c2cSellAdvertisement.setTradePrice(advertSellSimpleDto.getTradePrice());
		c2cSellAdvertisement.setMinSellAmount(advertSellSimpleDto.getMinAmount());
		c2cSellAdvertisement.setMaxSellAmount(advertSellSimpleDto.getMaxAmount());
		c2cSellAdvertisement.setRemark(advertSellSimpleDto.getRemark());
		c2cSellAdvertisement.setStatus(C2cSellAdvertStatus.SHOW);
		c2cSellAdvertisement.setLockNum(advertSellSimpleDto.getMaxAmount());
		c2cSellAdvertisement.setFee(BigDecimal.ZERO);
		//发布售出广告
		c2cSellAdvertisementService.deployC2cSellAdvertisementByUid(c2cSellAdvertisement, advertSellSimpleDto.getPayType());
	}

	//确定编辑广告
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/ensure-edit", method = RequestMethod.POST)
	public void ensureEdit(@AuthForHeader AuthContext context,@RequestBody C2cAdvertSellDeployInfoDto advertSellSimpleDto) {
		Integer uid = context.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		//校验是否设置过昵称
		if(Strings.isNullOrEmpty(user.getNickname())) {
			throw new AppException(C2cCodeConst.NO_SET_NICKNAME);
		}
		//校验是否设置过手机号
		if(Strings.isNullOrEmpty(user.getMobile())) {
			throw new AppException(C2cCodeConst.NO_SET_PHONE);
		}
		//校验交易币种类型
		if (!c2cAssetCodeConfigService.checkStatusListedAssetCodeByType(advertSellSimpleDto.getAssetcode(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_ASSET_CODE_INVALID);
		}
		//校验国家类型
		if (!c2cCountryConfigService.checkStatusListedCountryByType(advertSellSimpleDto.getCountry(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_COUNTRY_INVALID);
		}
		//校验货币类型
		if (!c2cCurrencyConfigService.checkStatusListedCurrencyByType(advertSellSimpleDto.getCurrency(), C2cTransType.SELL)) {
			throw new AppException(C2cCodeConst.ADVERT_CURRENCY_INVALID);
		}
		//交易价格零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getTradePrice())) {
			throw new AppException(C2cCodeConst.ADVERT_TRADE_PRICE_LESS_ZERO);
		}
		//最小售出个数零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getMinAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MIN_AMOUNT_LESS_ZERO);
		}
		//最大售出个数零值校验
		if(BigDecimalUtils.isLessZero(advertSellSimpleDto.getMaxAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_LESS_ZERO);
		}
		//校验最大售出个数是否小于最小售出个数
		if(BigDecimalUtils.isBigger(advertSellSimpleDto.getMinAmount(), advertSellSimpleDto.getMaxAmount())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_IS_LESS_MIN_AMOUNT);
		}
		Finance finance = financeService.queryAccount(uid, advertSellSimpleDto.getAssetcode());
		//空指针异常校验
		if(finance == null) {
			throw new AppException(C2cCodeConst.QUERY_USER_FINANCE_EXCEPTION);
		}
		//校验最大售出个数是否超过资产余额
		if(BigDecimalUtils.isBigger(advertSellSimpleDto.getMaxAmount(), finance.getAmountAvailable())) {
			throw new AppException(C2cCodeConst.ADVERT_MAX_AMOUNT_IS_BIGGER_FINANCE);
		}
		//校验是否绑定了对应的支付通道
		for(C2cPayType c2cPayType : advertSellSimpleDto.getPayType()) {
			switch(c2cPayType.name()) {
			case "ALIPAY":
				C2cAlipayInfo  c2cAlipayInfo = c2cAlipayInfoService.selectByUid(uid);
				if (c2cAlipayInfo == null) {
					throw new AppException(C2cCodeConst.NO_SET_ALIPAY_INFO);
				}
			case "BANK":
				C2cBankInfo c2cBankInfo = c2cBankInfoService.selectByUid(uid);
				if (c2cBankInfo == null) {
					throw new AppException(C2cCodeConst.NO_SET_BANK_INFO);
				}
			case "WECHAT":
				C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(uid);
				if (info == null) {
					throw new AppException(C2cCodeConst.NO_SET_WECHAT_INFO);
				}
			}
		}
		C2cSellAdvertisement c2cSellAdvertisement = new C2cSellAdvertisement();
		c2cSellAdvertisement.setUid(uid);
		c2cSellAdvertisement.setAdvertId(advertSellSimpleDto.getAdvertId());
		c2cSellAdvertisement.setNickname(user.getNickname());
		c2cSellAdvertisement.setAssetCode(advertSellSimpleDto.getAssetcode());
		c2cSellAdvertisement.setCountry(advertSellSimpleDto.getCountry());
		c2cSellAdvertisement.setPhone(user.getMobile());
		c2cSellAdvertisement.setCurrency(advertSellSimpleDto.getCurrency());
		c2cSellAdvertisement.setTradePrice(advertSellSimpleDto.getTradePrice());
		c2cSellAdvertisement.setMinSellAmount(advertSellSimpleDto.getMinAmount());
		c2cSellAdvertisement.setMaxSellAmount(advertSellSimpleDto.getMaxAmount());
		c2cSellAdvertisement.setRemark(advertSellSimpleDto.getRemark());
		c2cSellAdvertisement.setStatus(C2cSellAdvertStatus.SHOW);
		c2cSellAdvertisement.setLockNum(advertSellSimpleDto.getMaxAmount());
		c2cSellAdvertisement.setFee(BigDecimal.ZERO);
		c2cSellAdvertisementService.ensureEditC2cSellAdvertisementByUid(c2cSellAdvertisement, advertSellSimpleDto.getPayType());
	}

	//售出广告编辑页面展示
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/advert-edit", method = RequestMethod.GET)
	public C2cSellAdvertEditDto advertEdit(@AuthForHeader AuthContext context,@RequestParam("advertId") String advertId) {
		C2cSellAdvertisement c2cSellAdvert = c2cSellAdvertisementService.selectByAdvertId(advertId);
		if (c2cSellAdvert == null) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_EXIST);
		}
		Integer uid = context.getLoginSession().getUserId();
		//校验广告是否属于该用户
		if(!Objects.equals(uid, c2cSellAdvert.getUid())) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_BELONG_TO_USER);
		}
		//校验广告是否被删除
		if(C2cSellAdvertStatus.DELETED.equals(c2cSellAdvert.getStatus())) {
			//该广告已过期
			throw new AppException(C2cCodeConst.ADVERT_HAS_OVERDUE);
		}
		//校验广告是否已被购买
		if(C2cSellAdvertStatus.PURCHASED.equals(c2cSellAdvert.getStatus())) {
			throw new AppException(C2cCodeConst.ADVERT_HAS_PURCHASED);
		}
		C2cSellAdvertEditDto c2cSellAdvertEditDto = C2cSellAdvertEditDto.builder()
				.assetcodeList(c2cAssetCodeConfigService.getC2cAssetCodeConfig(C2cTransType.BUY))
				.countryList(c2cCountryConfigService.getC2cCountryConfig(C2cTransType.BUY))
				.currencyList(c2cCurrencyConfigService.getC2cCurrencyConfig(C2cTransType.BUY))
				.nickName(c2cSellAdvert.getNickname())
				.assetcode(c2cSellAdvert.getAssetCode())
				.country(c2cSellAdvert.getCountry())
				.currency(c2cSellAdvert.getCurrency())
				.phone(c2cSellAdvert.getPhone())
				.tradePrice(c2cSellAdvert.getTradePrice())
				.minAmount(c2cSellAdvert.getMinSellAmount())
				.maxAmount(c2cSellAdvert.getMaxSellAmount())
				.remark(c2cSellAdvert.getRemark()).build();
		return c2cSellAdvertEditDto;
	}

	//删除我的售出广告
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/advert-remove", method = RequestMethod.GET)
	public void advertRemove(@AuthForHeader AuthContext context,
			@RequestParam("advertId") String advertId) {
		Integer uid = context.getLoginSession().getUserId();
		c2cSellAdvertisementService.deleteC2cSellAdvertisementByAdvertId(advertId,uid);
	}

	//发布售出广告页面初始化信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/get-info", method = RequestMethod.GET)
	public C2cAdvertSellDto getInfo(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		C2cBankInfo c2cBankInfo = c2cBankInfoService.selectByUid(uid);
		C2cAlipayInfo c2cAlipayInfo = c2cAlipayInfoService.selectByUid(uid);
		C2cWeChatInfo c2cWeChatInfo  = c2cWechatService.selectStatusUsingByUid(uid);
		if(c2cWeChatInfo ==null) {
			c2cWeChatInfo = new C2cWeChatInfo();
		}
		C2cAlipayInfoDto c2cAlipayInfoDto = C2cAlipayInfoDto.builder()
				.uid(c2cAlipayInfo.getUid())
				.alipayNo(c2cAlipayInfo.getAlipayNo())
				.name(c2cAlipayInfo.getName())
				.createDate(c2cAlipayInfo.getCreateDate())
				.updateDate(c2cAlipayInfo.getUpdateDate())
				.build();
		c2cAlipayInfoDto.setC2cPayType(C2cPayType.ALIPAY);

		C2cBankInfoDto c2cBankInfoDto = C2cBankInfoDto.builder()
				.bank(c2cBankInfo.getBank())
				.subBank(c2cBankInfo.getSubbank())
				.acnumber(c2cBankInfo.getAcnumber())
				.name(c2cBankInfo.getName())
				.build();
		c2cBankInfoDto.setC2cPayType(C2cPayType.BANK);

		User userinfo = userFacade.getUser(uid);
		C2cWechatpayInfoDto C2cWeChatInfoDto = C2cWechatpayInfoDto.builder()
				.uid(c2cWeChatInfo.getUid())
				.tag(c2cWeChatInfo.getTag())
				.name(userinfo.getFullname())
				.createDate(c2cWeChatInfo.getCreateDate())
				.updateDate(c2cWeChatInfo.getUpdateDate())
				.build();
		C2cWeChatInfoDto.setC2cPayType(C2cPayType.WECHAT);

		List<C2cBasePayChannelDto> c2cBasePayChannelDtos = Lists.newArrayList();
		c2cBasePayChannelDtos.add(c2cAlipayInfoDto);
		c2cBasePayChannelDtos.add(c2cBankInfoDto);
		c2cBasePayChannelDtos.add(C2cWeChatInfoDto);

		//将交易币种、国家、货币列表、付款方式存入dto
		C2cAdvertSellDto c2cAdvertSellDto = C2cAdvertSellDto.builder()
				.assetcodeList(c2cAssetCodeConfigService.getC2cAssetCodeConfig(C2cTransType.SELL))
				.countryList(c2cCountryConfigService.getC2cCountryConfig(C2cTransType.SELL))
				.currencyList(c2cCurrencyConfigService.getC2cCurrencyConfig(C2cTransType.SELL))
				.c2cBasePayChannelDto(c2cBasePayChannelDtos)
				.build();
		return c2cAdvertSellDto;
	}

	//售出广告信息展示
	@RequestMapping(value = "/advert-query", method = RequestMethod.GET)
	public PageModel<C2cAdvertSellSimpleDto> advertQuery(@RequestParam("assetCode") String assetCode,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
		List<C2cSellAdvertisement> c2cSellAdverts = c2cSellAdvertisementService.selectAllByAssetCode(assetCode,pageNo,pageSize);

		if (null == c2cSellAdverts || c2cSellAdverts.isEmpty()) {
			return new PageModel<>();
		}
		List<C2cAdvertSellSimpleDto> advertSellSimpleDtos = Lists.newArrayList();
		for(C2cSellAdvertisement c2cSellAdvert : c2cSellAdverts) {
			C2cAdvertSellSimpleDto advertSellSimpleDto = C2cAdvertSellSimpleDto.builder()
					.advertId(c2cSellAdvert.getAdvertId())
					.assetcode(c2cSellAdvert.getAdvertId())
					.country(c2cSellAdvert.getCountry())
					.currency(c2cSellAdvert.getCurrency())
					.tradePrice(c2cSellAdvert.getTradePrice())
					.minAmount(c2cSellAdvert.getMinSellAmount())
					.maxAmount(c2cSellAdvert.getMaxSellAmount())
					.minMoney(c2cSellAdvert.getMinSellAmount().multiply(c2cSellAdvert.getTradePrice()).setScale(2,BigDecimal.ROUND_DOWN))
					.maxMoney(c2cSellAdvert.getMaxSellAmount().multiply(c2cSellAdvert.getTradePrice()).setScale(2,BigDecimal.ROUND_DOWN))
					.remark(c2cSellAdvert.getRemark())
					.payType(c2cOrderPaymentDetailService.selectPayTypeByAdvertId(c2cSellAdvert.getAdvertId()))
					//存入根据uid查询 交易次数 被鼓励次数 手机号 身份认证等级
					.c2cTransactionInfoDto(c2cUserTransactionInfoService.getUserTransactionInfo(c2cSellAdvert.getUid()))
					.build();
			advertSellSimpleDtos.add(advertSellSimpleDto);
		}
		PageInfo<C2cSellAdvertisement> pageInfo = new PageInfo<>(c2cSellAdverts);
		PageModel<C2cAdvertSellSimpleDto> pageModel = new PageModel<>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(advertSellSimpleDtos);
		return pageModel;
	}

	//买家查看售出广告详情
	@RequestMapping(value = "/advert-detail", method = RequestMethod.GET)
	public C2cAdvertSellBasicInfoDto advertDetail(@AuthForHeader AuthContext context,@RequestParam("advertId") String advertId) {
		C2cSellAdvertisement c2cSellAdvertisement = c2cSellAdvertisementService.selectByAdvertId(advertId);
		if(c2cSellAdvertisement == null) {
			throw new AppException(C2cCodeConst.ADVERT_NOT_EXIST);
		}
		if(C2cSellAdvertStatus.DELETED.equals(c2cSellAdvertisement.getStatus())) {

			throw new AppException(C2cCodeConst.ADVERT_HAS_DELETED);
		}
		if(C2cSellAdvertStatus.PURCHASED.equals(c2cSellAdvertisement.getStatus())) {

			throw new AppException(C2cCodeConst.ADVERT_HAS_PURCHASED);
		}
		//将该售出广告可支持的付款方式存入dto
		List<C2cOrderPaymentDetail> c2cOrderPaymentDetails = c2cOrderPaymentDetailService.selectDetailByAdvertId(advertId);
		List<C2cPayType> c2cPayTypes = Lists.newArrayList();
		for (C2cOrderPaymentDetail c2cOrderPaymentDetail : c2cOrderPaymentDetails) {
			c2cPayTypes.add(c2cOrderPaymentDetail.getPayType());
		}
		C2cAdvertSellBasicInfoDto c2cAdvertSellBasicInfoDto = C2cAdvertSellBasicInfoDto.builder()
				.nickName(c2cSellAdvertisement.getNickname())
				.assetcode(c2cSellAdvertisement.getAssetCode())
				.country(c2cSellAdvertisement.getCountry())
				.currency(c2cSellAdvertisement.getCurrency())
				.phone(c2cSellAdvertisement.getPhone())
				.tradePrice(c2cSellAdvertisement.getTradePrice())
				.minAmount(c2cSellAdvertisement.getMinSellAmount())
				.maxAmount(c2cSellAdvertisement.getMaxSellAmount())
				.minMoney(c2cSellAdvertisement.getMinSellAmount().multiply(c2cSellAdvertisement.getTradePrice()).setScale(2,BigDecimal.ROUND_DOWN))
				.maxMoney(c2cSellAdvertisement.getMaxSellAmount().multiply(c2cSellAdvertisement.getTradePrice()).setScale(2,BigDecimal.ROUND_DOWN))
				.remark(c2cSellAdvertisement.getRemark())
				.payType(c2cPayTypes)
				.advertId(advertId)
				.status(c2cSellAdvertisement.getStatus())
				.uid(c2cSellAdvertisement.getUid())
				.build();
		return c2cAdvertSellBasicInfoDto;
	}
	//查询我的售出广告
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/myadvert-query", method = RequestMethod.GET)
	public PageModel<C2cAdvertSellBasicInfoDto> myAdvertQuery(@AuthForHeader AuthContext context,
			@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
		Integer uid = context.getLoginSession().getUserId();
		List<C2cSellAdvertisement> c2cSellAdverts = c2cSellAdvertisementService.selectC2cSellAdvertByUid(uid,pageNo,pageSize);
		if (null == c2cSellAdverts || c2cSellAdverts.isEmpty()) {
			return new PageModel<>();
		}
		List<C2cAdvertSellBasicInfoDto> c2cAdvertSellBasicInfoDtos = Lists.newArrayList();
		for (C2cSellAdvertisement c2cSellAdvert : c2cSellAdverts) {
			C2cAdvertSellBasicInfoDto c2cAdvertSellBasicInfoDto = C2cAdvertSellBasicInfoDto.builder()
					.nickName(c2cSellAdvert.getNickname())
					.assetcode(c2cSellAdvert.getAssetCode())
					.country(c2cSellAdvert.getCountry())
					.currency(c2cSellAdvert.getCurrency())
					.phone(c2cSellAdvert.getPhone())
					.tradePrice(c2cSellAdvert.getTradePrice())
					.minAmount(c2cSellAdvert.getMinSellAmount())
					.maxAmount(c2cSellAdvert.getMaxSellAmount())
					.minMoney(c2cSellAdvert.getMinSellAmount().multiply(c2cSellAdvert.getTradePrice()))
					.maxMoney(c2cSellAdvert.getMaxSellAmount().multiply(c2cSellAdvert.getTradePrice()))
					.remark(c2cSellAdvert.getRemark())
					.advertId(c2cSellAdvert.getAdvertId())
					.status(c2cSellAdvert.getStatus())
					.createDate(c2cSellAdvert.getCreateDate())
					.build();
			c2cAdvertSellBasicInfoDtos.add(c2cAdvertSellBasicInfoDto);
		}

		PageInfo<C2cSellAdvertisement> pageInfo = new PageInfo<>(c2cSellAdverts);
		PageModel<C2cAdvertSellBasicInfoDto> pageModel = new PageModel<C2cAdvertSellBasicInfoDto>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(c2cAdvertSellBasicInfoDtos);
		return pageModel;
	}
}
