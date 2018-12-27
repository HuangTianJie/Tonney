package com.gop.certification.controller;


import javax.validation.Valid;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.authentication.dto.UserAuthLevel0Dto;
import com.gop.authentication.facade.AuthenticationFacade;
import com.gop.authentication.service.UserBasicInfoService;
import com.gop.authentication.service.UserIdentificationService;
import com.gop.authentication.service.UserResidenceService;
import com.gop.certification.dto.ResidenceAuthenticationDto;
import com.gop.certification.dto.UserInfoPageDto;
import com.gop.domain.Request;
import com.gop.domain.Response;
import com.gop.domain.User;
import com.gop.domain.UserBasicInfo;
import com.gop.domain.UserIdentification;
import com.gop.domain.UserResidence;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@RestController("overseasCertificationController")
@RequestMapping("/certification/overseas")
@Slf4j
public class CertificationController {
	// 用户基本认证信息提交

	@Autowired
	private UserFacade userFacade;

	// @Autowired
	// private VerifyService verifyService;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	@Autowired
	private UserBasicInfoService userBasicInfoService;

	@Autowired
	private UserIdentificationService userIdentificationService;

	@Autowired
	private UserResidenceService userResidenceService;

	@Autowired
	private Gson gson;

	@Autowired
	private UserService userService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	// 用户level0升级level1认证信息提交
	// 原用户基础信息与身份认证合并
	// 1.5.1版本添加
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/level1-authentication", method = RequestMethod.POST)
	public void levelOneAuthentication(@AuthForHeader AuthContext context, @Valid @RequestBody UserAuthLevel0Dto dto) {

		Integer uid = context.getLoginSession().getUserId();
		//为属性设置uid
		dto.getIdentificationDto().setUid(uid);
		dto.getUserBasicInfoDto().setUid(uid);
		authenticationFacade.commitUserLevel1Info(dto);
		// userFacade.saveUserUpAuteLevelToOneInfo(dto, uid);

	}

	// 用户level1升级level2认证信息提交
	// 1.5.1版本添加
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/level2-authentication", method = RequestMethod.POST)
	public void levelOneAuthentication(@AuthForHeader AuthContext context,
			@Valid @RequestBody ResidenceAuthenticationDto dto) {
		Integer uid = context.getLoginSession().getUserId();
		UserResidence userResidence = dto.dtoToDomain(dto);
		// 设置uid值
		userResidence.setUid(uid);
		// 数据库不能为null,所以设置0
		userResidence.setAuditUid(0);
		userResidence.setAuditMessage("");
		userResidence.setAuditMessageId("0");
		authenticationFacade.commUserLevel2Info(userResidence);
		// userFacade.saveUserUpAuteLevelToOneInfo(dto, uid);
	}

	// 用户认证查询状态接口
	@RequestMapping(value = "/basic-authentication-info", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public UserInfoPageDto getBaseAuthenticationInfo(@AuthForHeader AuthContext context) {

		Integer uid = context.getLoginSession().getUserId();
		//		log.info("user_id：{}", uid);
		UserSimpleInfoDto userInfo = userFacade.getUserInfoByUid(uid);
		//		log.info("userInfoByUid=" + userInfo.toString());
		String email = userInfo.getUserAccount();
		UserBasicInfo basicInfoByUid = userBasicInfoService.getBasicInfoByUid(uid);
		UserIdentification userIdentificationInfo = userIdentificationService.getLastUserIdentificationInfo(uid);
		UserResidence userResidenceInfo = userResidenceService.getLastUserResidenceInfo(uid);
		UserInfoPageDto userInfoPageDto = new UserInfoPageDto();
		userInfoPageDto.setEmail(email);
		userInfoPageDto.setUserBasicInfo(basicInfoByUid);
		userInfoPageDto.setUserIdentification(userIdentificationInfo);
		userInfoPageDto.setUserResidence(userResidenceInfo);
		return userInfoPageDto;
	}

	//web回调 插数据
	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public Response<String> excallback(@RequestParam("uid") Integer uid) {
		return userIdentificationService.faceCallBack(uid.toString());
	}
	
	//face++回调
	@RequestMapping(value = "/facecallback", method = RequestMethod.POST)
	public void facecallback() {
		System.out.print("face++认证结束");
	}

	//获取token
	@RequestMapping(value = "/territory", method = RequestMethod.GET)
	public Response<String> territory(@RequestParam("account") String account) {
		//根据登录账号查询uid
		User info = null;
		info = userService.getUserByEmail(account);
		if(info == null) {
			info = userService.getUserLikePhone(account);
		}
		return userIdentificationService.territory(info.getUid());
	}


	//web轮询认证结果
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	public String  getUserInfo(@RequestParam("account") String account) {
		//根据登录账号查询uid
		User info = null;
		info = userService.getUserByEmail(account);
		if(info == null) {
			info = userService.getUserLikePhone(account);
		}
		UserIdentification user = null;
		try {
			user = userIdentificationService.getLastUserIdentificationInfo(info.getUid());
			return user.getAuditStatus().toString();
		}catch (Exception e) {
			return "INIT";
		}
	}
}