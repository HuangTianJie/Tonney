package com.gop.currency.transfer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.dto.UserBankDto;
import com.gop.currency.transfer.dto.AcAccountDto;
import com.gop.currency.transfer.factory.ChannelUserAccountServiceFactory;
import com.gop.domain.enums.UserAccountChannelType;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController
@RequestMapping("/currency/account")
public class UserBindingAccountController {

	@Autowired
	private ChannelUserAccountServiceFactory channelUserAccountServiceFactory;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/user-accounts", method = RequestMethod.GET)
	// @ApiOperation("用户绑定银行卡信息")
	public PageModel<AcAccountDto> userAcBank(@AuthForHeader AuthContext context, @RequestParam("uid") Integer uid,
			@RequestParam("channelType") String bankType, @RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNo") Integer pageNo) {

		return channelUserAccountServiceFactory.getUserAccountService(UserAccountChannelType.valueOf(bankType))
				.getList(uid, pageNo, pageSize);
	}

}
