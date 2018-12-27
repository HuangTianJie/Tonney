package com.gop.test.controller;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.domain.TradeOrder;
import com.gop.match.dto.MatchOrderDetail;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController
@Slf4j
public class TestController {
	
	
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/test/find", method = RequestMethod.POST)
	public PageModel<MatchOrderDetail> getHistoryMatchOrder(@AuthForHeader AuthContext authContext,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("symbol") String symbol,
			@RequestParam("pageSize") Integer pageSize) {
		PageModel<MatchOrderDetail> pageModelDto = null;
	
		return pageModelDto;

	}

}
