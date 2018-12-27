package com.gop.currency.transfer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.currency.transfer.dto.ChannelConfigDetailDto;
import com.gop.currency.transfer.dto.ChannelConfigDto;
import com.gop.currency.transfer.dto.ChannelRulesDetailDto;
import com.gop.currency.transfer.dto.ChannelRulesDto;
import com.gop.currency.transfer.factory.ManagerDepositAccountFactory;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController("managerChannelAcManagerController")
@RequestMapping("/currency/channel")
@Slf4j
public class ChannelDepositManagerController {

	@Autowired
	private ManagerDepositAccountFactory factory;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/rules", method = RequestMethod.POST)
	// @ApiOperation("添加或修改收款账号的规则")
	public void addRule(@AuthForHeader AuthContext context, @Validated @RequestBody ChannelRulesDto dto) {

		int adminId = context.getLoginSession().getUserId();

		log.info("id:" + dto.getId());
		log.info("account:" + dto.getAccount());
		log.info("name:" + dto.getName());
		log.info("code:" + dto.getCode());
		log.info("rules:" + dto.getRules());
		log.info("remark:" + dto.getRemark());
		factory.getService(dto.getChannelType().toString()).replace(dto.getId(), dto.getAccount(), dto.getName(),
				dto.getCode(), dto.getRules(), dto.getStatus(), dto.getRemark(), adminId);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/rules", method = RequestMethod.GET)
	// @ApiOperation("删除收款账号的收款规则")
	public void deletSeRule(@AuthForHeader AuthContext context, @RequestParam("id") Integer id,
			@RequestParam("channelType") String channelType) {

		factory.getService(channelType).delete(id);
	}
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/rules-list", method = RequestMethod.GET)
	// @ApiOperation("查询收款账号的收款规则")
	public List<ChannelRulesDetailDto> rulesList(@AuthForHeader AuthContext context,
			@RequestParam("channelType") String channelType) {
		return factory.getService(channelType).getRulesList();
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/channel-config", method = RequestMethod.POST)
	// @ApiOperation("设置渠道收款规则")
	public void addAlipayConfig(@AuthForHeader AuthContext context, @Validated @RequestBody ChannelConfigDto dto) {
		int adminId = context.getLoginSession().getUserId();
		factory.getService(dto.getChannelType()).addAndUpdateGlobalConfig(dto.getId(), dto.getMinAmount(),
				dto.getMaxAmount(), dto.getStatus(), adminId);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/channel-config", method = RequestMethod.GET)
	public ChannelConfigDetailDto getAlipayConfig(@AuthForHeader AuthContext context,
			@RequestParam("channelType") String channelType) {
		return factory.getService(channelType).getGlobalConfig();
	}

}
