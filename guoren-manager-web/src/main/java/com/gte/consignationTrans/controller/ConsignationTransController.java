package com.gte.consignationTrans.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.gop.common.CheckCodeService;
import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import com.gop.match.service.MatchOrderService;
import com.gop.mode.vo.PageModel;
import com.gte.consignationTrans.dto.MatchOrderDetail;
import com.gte.countryarea.service.ConsignationTransService;

@Slf4j
@RestController
@RequestMapping("/consignationTrans")
public class ConsignationTransController {


	@Autowired
	private Gson gson;

	@Autowired
	private ConsignationTransService consignationTransService;

	@Autowired
	MatchOrderService matchOrderService;
	
	@Autowired
	private CheckCodeService checkCodeService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public PageModel<MatchOrderDetail> getUserDetail(
			@RequestParam(value = "uid", required = false) Integer uId,
			@RequestParam(value = "symbol", required = false) String symbol,
			@RequestParam(value = "outerOrderNo", required = false) String outerOrderNo,
			@RequestParam(value = "orderType", required = false) TradeCoinType orderType,
			@RequestParam(value = "status", required = false) TradeCoinStatus status,
			@RequestParam(value = "sortProp", required = false) String sortProp,
			@RequestParam(value = "sortOrder", required = false) String sortOrder,
			@RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
			@RequestParam(value = "endTime", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {
		
		String orderBy = null;
		if(sortProp == null || sortOrder == null){
			orderBy = "create_date desc";
		}else{
			orderBy = sortProp+" "+sortOrder;
		}

		PageModel<MatchOrderDetail> pageModelDto = null;
		PageModel<TradeOrder> pageModel = matchOrderService.queryConsignation(uId, symbol, outerOrderNo, orderType, status, startDate, endDate, pageNo, pageSize,orderBy);
		pageModelDto = new PageModel<MatchOrderDetail>();
		if (null != pageModel) {
			
			List<MatchOrderDetail> lists = pageModel.getList().stream().map(a -> new MatchOrderDetail(a))
					.collect(Collectors.toList());
			pageModelDto.setPageNo(pageModel.getPageNo());
			pageModelDto.setPageNum(pageModel.getPageNum());
			pageModelDto.setPageSize(pageModel.getPageSize());
			pageModelDto.setList(lists);
			
			int total = matchOrderService.querytotal();
			
			pageModelDto.setTotal((long)total);
			
			int totaldealed = matchOrderService.querytotaldealed();
			int totaldealing = matchOrderService.querytotaldealing();
			
			pageModelDto.setTotaldealed(totaldealed);
			pageModelDto.setTotaldealing(totaldealing);
		}

		return pageModelDto;
	}
}
