package com.gop.c2c.normaltransaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.gop.c2c.dto.C2cAlipayInfoDto;
import com.gop.c2c.dto.C2cBankInfoDto;
import com.gop.c2c.dto.C2cTransOrderDto;
import com.gop.c2c.service.C2cAlipayInfoService;
import com.gop.c2c.service.C2cBankInfoService;
import com.gop.c2c.service.C2cOrderPaymentDetailService;
import com.gop.c2c.service.C2cTransOrderService;
import com.gop.config.CommonConfig;
import com.gop.domain.C2cAlipayInfo;
import com.gop.domain.C2cBankInfo;
import com.gop.domain.C2cOrderPaymentDetail;
import com.gop.domain.C2cTransOrder;
import com.gop.domain.enums.C2cPayType;
import com.gop.domain.enums.C2cTransOrderStatus;
import com.gop.domain.enums.C2cTransType;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("C2ctransactionorderinfo")
@RequestMapping("/c2ctransactionorderinfo")
public class C2ctransactionorderinfoController {
	@Autowired
	private C2cTransOrderService c2cTransOrderService;

	@Autowired
	private C2cAlipayInfoService c2cAlipayInfoService;
	@Autowired
	private C2cBankInfoService c2cBankInfoService;
	@Autowired
	private C2cOrderPaymentDetailService c2cOrderPaymentDetailService;

	@RequestMapping(value = "/order-query", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<C2cTransOrderDto> orderQuery(@AuthForHeader AuthContext context,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize,
			@RequestParam(value = "status", required = false) C2cTransOrderStatus status,
			@RequestParam(value = "orderType", required = false) C2cTransType orderType) {
		Integer uid = context.getLoginSession().getUserId();

		if (pageNo == null || pageSize == null) {
			pageNo = CommonConfig.DEFAULT_PAGE_NO;
			pageSize = CommonConfig.DEFAULT_PAGE_SIZE;
		}
		PageModel<C2cTransOrderDto> model = c2cTransOrderService.managerOrderList(pageNo, pageSize, status, uid,
				orderType);
		return model;
	}
}