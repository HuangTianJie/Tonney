package com.gop.user.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.common.Environment;
import com.gop.domain.User;
import com.gop.exception.AppException;
import com.gop.user.dto.UserBasicInfo;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserBasicController {

	@Autowired
	private UserService userService;

	@Autowired
	Environment environment;

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/basic", method = RequestMethod.GET)
	public UserBasicInfo getBasicInfo(@AuthForHeader AuthContext authContext) {

		int uid = authContext.getLoginSession().getUserId();

		User user = userService.getUserByUid(uid);

		if (user == null) {
			authContext.setLoginSession(null);
			throw new AppException(SecurityCodeConst.NO_LOGIN);
		}
		// 封装userBasicInfo给前端
		UserBasicInfo userBasicInfo = new UserBasicInfo();
		userBasicInfo.setAuthLevel(user.getAuthLevel().toString());
		userBasicInfo.setUid(user.getUid());
		userBasicInfo.setMobile(user.getMobile());
		userBasicInfo.setNickname(user.getNickname());
		userBasicInfo.setSetPayPassword(!Strings.isNullOrEmpty(user.getPayPassword()));
		if (!StringUtils.isEmpty(user.getFullname())) {
			userBasicInfo.setName(user.getFullname());
		}
		if (!StringUtils.isEmpty(user.getMobile())) {
			userBasicInfo.setMobile(user.getMobile());
		}
		String account = null;

		account = user.getEmail();

		userBasicInfo.setAccount(account);
		return userBasicInfo;

	}

}
