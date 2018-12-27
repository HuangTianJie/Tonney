package com.gop.c2c.userTransactionInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.gop.c2c.dto.C2cOtherBuyDto;
import com.gop.c2c.dto.C2cOtherSellDto;
import com.gop.c2c.dto.C2cTransactionInfoDto;
import com.gop.c2c.service.C2cBuyAdvertisementService;
import com.gop.c2c.service.C2cOrderPaymentDetailService;
import com.gop.c2c.service.C2cSellAdvertisementService;
import com.gop.c2c.service.C2cUserEncourageInfoRecordService;
import com.gop.c2c.service.C2cUserTransactionInfoService;
import com.gop.code.consts.C2cCodeConst;
import com.gop.domain.C2cBuyAdvertisement;
import com.gop.domain.C2cSellAdvertisement;
import com.gop.domain.enums.C2cBuyAdvertStatus;
import com.gop.domain.enums.C2cSellAdvertStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController("C2cUserTransactionInfoController")
@RequestMapping("/c2cbasicinfo")
@Slf4j
/**
 * 
 * @author zhangliwei
 *
 */
public class C2cUserTransactionInfoController {
	
	
	@Autowired
	@Qualifier("C2cUserTransactionInfoService")
	private C2cUserTransactionInfoService c2cUserTransactionInfoService;
	
	@Autowired
	private C2cSellAdvertisementService c2cSellAdvertisementService;
	
	@Autowired
	private C2cBuyAdvertisementService c2cBuyAdvertisementService;
	
	@Autowired
	private C2cOrderPaymentDetailService c2cOrderPaymentDetailService;
	
	@Autowired
	private C2cUserEncourageInfoRecordService c2cUserEncourageInfoRecordService;

	@RequestMapping(value = "/count-query", method = RequestMethod.GET)
	public C2cTransactionInfoDto getUserInfo(@RequestParam("uid") Integer uid) {
	
		C2cTransactionInfoDto c2cTransactionInfoDto = c2cUserTransactionInfoService.getUserTransactionInfo(uid);
		return c2cTransactionInfoDto;
	}
	//查询卖家其他的售卖信息
	@RequestMapping(value = "/advert-other-sell-query", method = RequestMethod.GET)
	public PageModel<C2cOtherSellDto> c2cOtherSell(@AuthForHeader AuthContext authContext,@RequestParam("advertId") String advertId,@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
		
		  C2cSellAdvertisement c2cSellAdvertisement = c2cSellAdvertisementService.selectByAdvertId(advertId);
		  if(c2cSellAdvertisement == null) {
			  
			  return new PageModel<>();
		  }
		  Integer uid = c2cSellAdvertisement.getUid();
		  String assetCode = c2cSellAdvertisement.getAssetCode();
		  List<C2cSellAdvertisement> c2cSellAdvertisements = c2cSellAdvertisementService.selectC2cSellAdvertByUidAndStatusAndAssetCode(uid, C2cSellAdvertStatus.SHOW,assetCode,pageNo, pageSize);
		  if (null == c2cSellAdvertisements || c2cSellAdvertisements.isEmpty()) {
			  
			  return new PageModel<>();
		  }
		  List<C2cOtherSellDto>  c2cOtherSellDtos = Lists.newArrayList();
		  for (C2cSellAdvertisement advertisement :c2cSellAdvertisements) {
			
			  if(!advertisement.getAdvertId().equals(advertId)) {
				  
				  C2cOtherSellDto c2cOtherSellDto = C2cOtherSellDto.builder().c2cPayType(c2cOrderPaymentDetailService.selectPayTypeByAdvertId(advertisement.getAdvertId())).tradePrice(advertisement.getTradePrice()).advertId(advertisement.getAdvertId()).build(); 
				  c2cOtherSellDtos.add(c2cOtherSellDto);
			  }
		  }
		  PageInfo<C2cSellAdvertisement> pageInfo = new PageInfo<>(c2cSellAdvertisements);
		  PageModel<C2cOtherSellDto> pageModel = new PageModel<C2cOtherSellDto>();
		  pageModel.setPageNo(pageNo);
		  pageModel.setPageNum(pageInfo.getPages());
		  pageModel.setPageSize(pageSize);
		  pageModel.setTotal(pageInfo.getTotal());
		  pageModel.setList(c2cOtherSellDtos);
		  return pageModel;
	}
	
	//查询买家其他的求购信息
	@RequestMapping(value = "/advert-other-buy-query", method = RequestMethod.GET)
	public PageModel<C2cOtherBuyDto> c2cOtherBuy(@AuthForHeader AuthContext authContext,@RequestParam("advertId") String advertId,@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {

		  C2cBuyAdvertisement c2cBuyAdvertisement = c2cBuyAdvertisementService.selectByAdvertId(advertId);
		  if(c2cBuyAdvertisement == null) {
			  
			  return new PageModel<>();
		  }
		  Integer uid = c2cBuyAdvertisement.getUid();
		  List<C2cBuyAdvertisement> c2cBuyAdvertisements = c2cBuyAdvertisementService.selectC2cBuyAdvertByUidAndStatus(uid, C2cBuyAdvertStatus.SHOW,pageNo, pageSize);
		  if (null == c2cBuyAdvertisements || c2cBuyAdvertisements.isEmpty()) {
				
			  return new PageModel<>();
		  }
		  List<C2cOtherBuyDto>  c2cOtherBuyDtos = Lists.newArrayList();
		  for (C2cBuyAdvertisement advertisement :c2cBuyAdvertisements) {
			  
			  if(!advertisement.getAdvertId().equals(advertId)) {
				  
				  C2cOtherBuyDto c2cOtherBuyDto = C2cOtherBuyDto.builder().tradePrice(advertisement.getTradePrice()).assetCode(advertisement.getAssetCode()).advertId(advertisement.getAdvertId()).build();
				  c2cOtherBuyDtos.add(c2cOtherBuyDto);
			  }
		  }
		  PageModel<C2cOtherBuyDto> pageModel = new PageModel<C2cOtherBuyDto>();
		  PageInfo<C2cBuyAdvertisement> pageInfo = new PageInfo<>(c2cBuyAdvertisements);
		  pageModel.setPageNo(pageNo);
		  pageModel.setPageNum(pageInfo.getPages());
		  pageModel.setPageSize(pageSize);
		  pageModel.setTotal(pageInfo.getTotal());
		  pageModel.setList(c2cOtherBuyDtos);
		  return pageModel;
	}
	
	//鼓励别人
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/encourage-add", method = RequestMethod.GET)
	public void  encourageOthers(@AuthForHeader AuthContext authContext,@RequestParam("encouragedUid") Integer encouragedUid) {
			
		int encourageId = authContext.getLoginSession().getUserId();
		Integer uid = encouragedUid;
		if(encourageId == uid) {
			
			throw new AppException(C2cCodeConst.ENCOURAGE_SELF);
		}
		Calendar currentDayBegin = new GregorianCalendar();
		currentDayBegin.set(Calendar.HOUR_OF_DAY, 0);
		currentDayBegin.set(Calendar.MINUTE, 0);
		currentDayBegin.set(Calendar.SECOND, 0);
		int encourageCount = c2cUserEncourageInfoRecordService.getTodayEncourageCountByEncourageId(uid, encourageId, currentDayBegin.getTime(), new Date());
		
		if(encourageCount >= 50) {
			
			throw new AppException(C2cCodeConst.ENCOURAGE_COUNT_MAX);
		}
		c2cUserEncourageInfoRecordService.addEncourage(uid, encourageId);	
	}	
}
