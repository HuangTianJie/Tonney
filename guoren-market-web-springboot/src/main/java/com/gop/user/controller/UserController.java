package com.gop.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.common.GetCountyAndCityByIPService;
import com.gop.common.SmsMessageService;
import com.gop.conetxt.EnvironmentContxt;
import com.gop.domain.*;
import com.gop.exception.AppException;
import com.gop.sms.dto.VerifyCodeDto;
import com.gop.sms.service.MessageGenerator;
import com.gop.user.dto.CheckLoginLockedDto;
import com.gop.user.dto.CheckPayPasswordLockedDto;
import com.gop.user.dto.UserDto;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.BrokerService;
import com.gop.user.service.UserPayPasswordService;
import com.gop.user.service.UserPaypasswordRelatationService;
import com.gop.user.service.UserService;
import com.gop.util.*;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController("demosticUserController")
@RequestMapping("/user")
@Slf4j
public class UserController {
	@Autowired
	@Qualifier("userService")
	private UserService userService;
	@Autowired
	private TokenHelper tokenHelper;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	private BrokerService brokerService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private SmsMessageService smsMessageService;

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private UserPayPasswordService userPayPasswordService;

	@Autowired
	private Gson gson;

	@Value("${email.urlDomain}")
	private String urlDomain;
	
	@Value("${service.url}")
	private String serviceUrl;


	private long expireTime = 15;

	private final String randomCodePrix = "RandomCode";

	private final String ipPrix = "ip:";
	@Autowired
	@Qualifier("verifyCodeMessageGenerator")
	private MessageGenerator<VerifyCodeDto> verifyCodeMessageGenerator;
	@Autowired
	EnvironmentContxt environmentContxt;


	@Autowired
	@Qualifier("getCountyAndCityByIPServiceImpl")
	private GetCountyAndCityByIPService getCountyAndCityByIPService;

	@Autowired
	private UserPaypasswordRelatationService userPaypasswordRelatationService;

	public static final Integer USER_HAS_LOCKED = 1;

	public static final Integer USER_UNLOCKED = 0;

	public static final Integer UER_MAX_LOCK_TIMES = 10;// 登录及修改密码共用

	public static final String LOGIN_SESSION_ID = "loginsession";
	public static final Executor executor = Executors.newSingleThreadExecutor();

	// 注册
	@RequestMapping(value = "/phone-register", method = RequestMethod.POST)
	public JSONObject phoneRegister(@AuthForHeader AuthContext authContext, @RequestBody UserDto userDto) {
		String userAccount = authContext.getUserAccount();
		log.info("手机注册手机号：{} 参数：{}",userAccount,gson.toJson(userDto));
		if (Strings.isNullOrEmpty(userAccount)) {
			log.info("无效的用户账户地址");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		//校验手机号是否注册
		User userinfo = userService.getUserLikePhone(userAccount);
		if(null != userinfo){
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}

		// 校验userDto参数
		if (userDto == null) {
			log.info("{}:userDto为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		String code = userDto.getCode();
		String loginPassword = userDto.getLoginPassword();
		Integer invitId = userDto.getInvitId();
		String nickname = userDto.getNickname();
		Integer brokerId = userDto.getBrokerId();
		String loginsalt = CryptoUtils.getSalt();

		String md5Pwd = MD5Util.genMD5Code(loginPassword);
		String pwd = CryptoUtils.getHash(md5Pwd, loginsalt);


		String key = Joiner.on(":").join(userAccount, "code");
		String key2 = Joiner.on(":").join(userAccount, "avoid");
		if (StringUtils.isEmpty(code)||!Joiner.on(":").join(code,userAccount).equals(redisTemplate.opsForValue().get(key))) {
			log.info("输入验证码:{},发送验证码：{}",code,redisTemplate.opsForValue().get(key));
			throw new AppException(CommonCodeConst.CODE_ERROR);
		}
		if (StringUtils.isEmpty(loginPassword)) {
			log.info("{}:loginPassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (loginPassword.contains(" ")) {
			log.info("{}:paypassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		if (brokerId == null) {
			log.info("{}:brokerId为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		// 查询broker表
		Broker brokerByBrokerId = brokerService.getBrokerByBrokerId(Long.valueOf(brokerId));
		if (brokerByBrokerId == null) {
			log.info("userDto的brokerId在broker表找不到记录");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		// 校验登录密码及支付密码
		loginPassword = ConstantUtil.charConvert(loginPassword);

		if (loginPassword.length() < 6 || loginPassword.length() > 20) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		if (!PasswordUtil.checkPasswordFormat(loginPassword)) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		Boolean isReg = userService.isPhoneRegister(userAccount);
		if (isReg) {
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}

		User user = userFacade.reRegistrationUserCreate(null, userAccount, pwd, loginsalt, invitId, nickname, brokerId);
		//插入资金密码表
		//查询uid
		User info = userService.getUserByPhone(userAccount);
		UserPaypasswordRelationship param = new UserPaypasswordRelationship();
		param.setUid(info.getUid());
		param.setInputType(0);
		param.setUpdateDate(new Date());
		userPaypasswordRelatationService.insertInfo(param);

		redisTemplate.delete(key);
		redisTemplate.delete(key2);

		Integer uid = user.getUid();

		// 注册成功 存储到session中

		JSONObject json = new JSONObject();
		String token = tokenHelper.generateToken(uid);
		json.put("token", token);
		return json;
	}

	// 验证手机号码是否已经注册

	@RequestMapping(value = "/phone-register-valid", method = RequestMethod.GET)
	public void isPhoneRegister(@RequestParam("phone") String phone) {
		boolean isRegistered = false;

		isRegistered = userService.isPhoneRegister(phone);

		if (isRegistered) {
			throw new AppException(UserCodeConst.HAS_REGISTER);
		} else {

			throw new AppException(UserCodeConst.NO_REGISTER);

		}
	}

	// 注册
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})"))
	@RequestMapping(value = "/email-register", method = RequestMethod.POST)
	public JSONObject emailRegister(@AuthForHeader AuthContext authContext, @Valid @RequestBody UserDto userDto) {

		String userAccount = authContext.getUserAccount();

		// 校验userDto参数
		if (userDto == null) {
			log.error("userDto为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		String loginPassword = userDto.getLoginPassword();
		String payPassword = userDto.getPayPassword();
		Integer invitId = userDto.getInvitId();
		String nickname = userDto.getNickname();
		Integer brokerId = userDto.getBrokerId();

		if (StringUtils.isEmpty(loginPassword)) {
			log.error("loginPassword为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (StringUtils.isEmpty(payPassword)) {
			log.error("paypassword为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (brokerId == null) {
			log.error("brokerId为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (loginPassword.contains(" ")) {
			log.info("{}:paypassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (payPassword.contains(" ")) {
			log.info("{}:paypassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		// 查询broker表
		Broker brokerByBrokerId = brokerService.getBrokerByBrokerId(Long.valueOf(brokerId));
		if (brokerByBrokerId == null) {
			log.info("userDto的brokerId在broker表找不到记录");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		// 校验登录密码及支付密码
		loginPassword = ConstantUtil.charConvert(loginPassword);
		payPassword = ConstantUtil.charConvert(payPassword);

		if (loginPassword.length() < 6 || loginPassword.length() > 20) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		if (!PasswordUtil.checkPasswordFormat(loginPassword)) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		if (payPassword.length() < 8 || payPassword.length() > 20) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}

		if (!PasswordUtil.checkPasswordFormat(payPassword)) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}

		if (!EmailVerify.validEmailNumber(userAccount)) {
			throw new AppException(UserCodeConst.EMAIL_FORMAT_ERROR);
		}

		Boolean isReg = userService.isMailRegister(userAccount);
		if (isReg) {
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}

		userFacade.createUser(userAccount, null, loginPassword, payPassword, invitId, nickname, brokerId);
		User userByEmail = userService.getUserByEmail(userAccount);
		Integer uid = userByEmail.getUid();

		// 注册成功之后记录ip
		UserLoginLog userLoginLog = new UserLoginLog();
		userLoginLog.setUid(uid);
		final String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {

			log.info("无法从用户uid:" + uid + "请求header中的X-Real-IP获取真实IP地址");
			userLoginLog.setIpCountry("Unknow");
			userLoginLog.setIpCity("Unknow");
			userLoginLog.setIpAddress("Unknow");
			userService.recordUserLogin(userLoginLog);
		} else {
			CompletableFuture.runAsync(() -> {
				Map<String, String> map = null;
				try {
					map = getCountyAndCityByIPService.getCountyAndCityByIp(ip);
				} catch (Exception e) {
					userLoginLog.setIpCountry("Unknow");
					userLoginLog.setIpCity("Unknow");
				}
				if (null != map) {
					userLoginLog.setIpCountry(map.get("country"));
					userLoginLog.setIpCity(map.get("city"));
				}
				userLoginLog.setIpAddress(ip);
				userService.recordUserLogin(userLoginLog);
			});
		}

		JSONObject json = new JSONObject();
		String token = tokenHelper.generateToken(uid);
		json.put("token", token);
		return json;

	}

	// 预注册
	@RequestMapping(value = "/email-pre-register", method = RequestMethod.POST)
	public void emailPreRegister(@AuthForHeader AuthContext authContext, @Valid @RequestBody UserDto userDto) {
		String userAccount = authContext.getUserAccount();
		// 校验userDto参数
		if (userDto == null) {
			log.error("userDto为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {
			ip = "Unknow";
		}
		if(redisTemplate.hasKey(ipPrix+ip)) {
			int count = Integer.valueOf(redisTemplate.opsForValue().get(ipPrix+ip));
			if(count > 100) {
				throw new AppException(UserCodeConst.USER_REGISTER_TOO_FREQUENTLY);
			}
			redisTemplate.boundValueOps(ipPrix+ip).increment(1);
		}else {
			redisTemplate.opsForValue().set(ipPrix+ip, "1", 24 ,TimeUnit.HOURS);
		}

		String loginPassword = userDto.getLoginPassword();
		Integer invitId = userDto.getInvitId();
		String nickname = userDto.getNickname();
		Integer brokerId = userDto.getBrokerId();
		String lang = userDto.getLang();

		if (StringUtils.isEmpty(loginPassword)) {
			log.error("loginPassword为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		if (brokerId == null) {
			log.error("brokerId为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (loginPassword.contains(" ")) {
			log.info("{}:paypassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if(Strings.isNullOrEmpty(lang)) {
			lang="";
		}

		// 查询broker表
		Broker brokerByBrokerId = brokerService.getBrokerByBrokerId(Long.valueOf(brokerId));
		if (brokerByBrokerId == null) {
			log.info("userDto的brokerId在broker表找不到记录");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		// 校验登录密码及支付密码
		loginPassword = ConstantUtil.charConvert(loginPassword);

		if (loginPassword.length() < 6 || loginPassword.length() > 20) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		if (!PasswordUtil.checkPasswordFormat(loginPassword)) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		if (!EmailVerify.validEmailNumber(userAccount)) {
			throw new AppException(UserCodeConst.EMAIL_FORMAT_ERROR);
		}

		Boolean isReg = userService.isMailRegister(userAccount);
		if (isReg) {
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}
		//查询是否有开启的邀请活动
		//		if(inviteActivityConfigService.countInviteActivityConfigByStatus(InviteActivityConfigStatus.ON) >0) {
		//			InviteUserInfo inviteUserInfo  = inviteUserInfoService.getInviteUserInfoByInviteCode(userDto.getInviteCode());
		//			if (null != inviteUserInfo) {
		//				invitId = inviteUserInfo.getUid();
		//			}
		//		}
		UserPreRegistrationPool userPreRegistration = userService.addUserPerRegistrationInformation(userAccount, null, loginPassword, invitId, nickname, brokerId);
		String preRegistCount = userPreRegistration.getEmail();
		String randomCode=UUID.randomUUID().toString().trim().replaceAll("-", "");
		redisTemplate.opsForValue().set(randomCodePrix + ":" + preRegistCount, randomCode, expireTime, TimeUnit.MINUTES);

		String url = urlDomain + lang+"/active.html?userPreRegistrationId="+userPreRegistration.getId()+"&randomCode="+randomCode;
		smsMessageService.sendEmailMessage(userAccount, url,true,"registerActiveMessage_"+LocaleContextHolder.getLocale().toString()+".ftl");
	}


	//激活注册账号
	@RequestMapping(value = "/email-activate", method = RequestMethod.GET)
	public void preRegisterVerification(@AuthForHeader AuthContext authContext, @RequestParam("userPreRegistrationId") Integer userPreRegistrationId,@RequestParam("randomCode") String randomCode) {
		UserPreRegistrationPool userPreRegistration = userService.getUserPerRegistrationInformationById(userPreRegistrationId);
		String redisRandomKey = redisTemplate.opsForValue().get(randomCodePrix + ":" + userPreRegistration.getEmail());	
		//校验用户randomKey
		if (Strings.isNullOrEmpty(redisRandomKey)) {
			throw new AppException(UserCodeConst.PRE_REGISTRATION_LINK_TIMEOUT);
		}
		if (!randomCode.equals(redisRandomKey)) {
			throw new AppException(UserCodeConst.PRE_REGISTRATION_LINK_TIMEOUT);
		}
		String userAccount = userPreRegistration.getEmail();

		String loginPassword = userPreRegistration.getLoginPassword();
		Integer invitId = userPreRegistration.getInviteUid();
		String nickname = userPreRegistration.getNickname();
		Integer brokerId = userPreRegistration.getBrokerId();
		String loginsalt = userPreRegistration.getLoginSalt();
		if (StringUtils.isEmpty(loginPassword)) {
			log.error("loginPassword为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		if (brokerId == null) {
			log.error("brokerId为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (loginPassword.contains(" ")) {
			log.info("{}:paypassword为null", userAccount);
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		// 查询broker表
		Broker brokerByBrokerId = brokerService.getBrokerByBrokerId(Long.valueOf(brokerId));
		if (brokerByBrokerId == null) {
			log.info("userDto的brokerId在broker表找不到记录");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		Boolean isReg = userService.isMailRegister(userAccount);
		if (isReg) {
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}


		userFacade.reRegistrationUserCreate(userAccount, null, loginPassword, loginsalt, invitId, nickname, brokerId);
		User userByEmail = userService.getUserByEmail(userAccount);
		Integer uid = userByEmail.getUid();

		//插入资金密码表
		UserPaypasswordRelationship param = new UserPaypasswordRelationship();
		param.setUid(uid);
		param.setInputType(0);
		param.setUpdateDate(new Date());
		userPaypasswordRelatationService.insertInfo(param);

		// 注册成功之后记录ip
		UserLoginLog userLoginLog = new UserLoginLog();
		userLoginLog.setUid(uid);
		final String ip = request.getHeader("X-Real-IP");

		userService.updateCreateIpByUid(uid, Strings.isNullOrEmpty(ip) ? "Unknow" : ip);

		if (ip == null || ip.length() == 0) {

			log.info("无法从用户uid:" + uid + "请求header中的X-Real-IP获取真实IP地址");
			userLoginLog.setIpCountry("Unknow");
			userLoginLog.setIpCity("Unknow");
			userLoginLog.setIpAddress("Unknow");
			userService.recordUserLogin(userLoginLog);
		} else {
			CompletableFuture.runAsync(() -> {
				Map<String, String> map = null;
				try {
					map = getCountyAndCityByIPService.getCountyAndCityByIp(ip);
				} catch (Exception e) {
					userLoginLog.setIpCountry("Unknow");
					userLoginLog.setIpCity("Unknow");
				}
				if (null != map) {
					userLoginLog.setIpCountry(map.get("country"));
					userLoginLog.setIpCity(map.get("city"));
				}
				userLoginLog.setIpAddress(ip);
				userService.recordUserLogin(userLoginLog);
			});
		}     	     



	}
	// 验证邮箱是否已经注册
	@RequestMapping(value = "/email-register-valid", method = RequestMethod.GET)
	public void isEmailRegister(@RequestParam("email") String email) {
		boolean isRegistered = false;
		// 验证邮箱是否已注册service
		log.info("email={}", email);

		isRegistered = userService.isMailRegister(email);

		if (isRegistered) {
			// 邮箱已注册
			log.info("邮箱已注册:email={}", email);
			throw new AppException(UserCodeConst.HAS_REGISTER);
		} else {
			// 邮箱没注册
			log.info("邮箱没有注册:email={}", email);

		}
	}

	// 登录
	@ApiOperation(value = "用户登录")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query", name = "brokerId", value = "用户ID", required = true, dataType = "String",defaultValue="10003"  ),
			@ApiImplicitParam(paramType="header", name = "authorization", value = "令牌", required = true, dataType = "String",defaultValue="account-no=hfjinsong%40163.com,login-password=a123456789"  )
	})
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginPasswordWithLoginStrategy'}})"))
	public UserSimpleInfoDto login(@AuthForHeader AuthContext authContext) {
		User user = null;

		String userAccount = authContext.getUserAccount();

		user = userService.getUserByAccount(userAccount);
		// update user表 锁定记录清空
		User userNew = new User();
		userNew.setUid(user.getUid());
		userNew.setLockNum((byte) 0);
		userNew.setUpdateDate(new Date());
		userService.updateByPrimaryKeySelective(userNew);

		// 登录成功之后记录用户登录ip与时间
		UserLoginLog userLoginLog = new UserLoginLog();
		userLoginLog.setUid(user.getUid());
		final String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {

			log.info("无法从用户uid:" + userLoginLog.getUid() + "请求header中的X-Real-IP获取真实IP地址");
			userLoginLog.setIpCountry("Unknow");
			userLoginLog.setIpCity("Unknow");
			userLoginLog.setIpAddress("Unknow");
			userService.recordUserLogin(userLoginLog);
		} else {
			CompletableFuture.runAsync(() -> {
				Map<String, String> map = null;
				try {
					map = getCountyAndCityByIPService.getCountyAndCityByIp(ip);
				} catch (Exception e) {
					userLoginLog.setIpCountry("Unknow");
					userLoginLog.setIpCity("Unknow");
				}
				if (null != map) {
					userLoginLog.setIpCountry(map.get("country"));
					userLoginLog.setIpCity(map.get("city"));
				}
				userLoginLog.setIpAddress(ip);
				userService.recordUserLogin(userLoginLog);
			});
		}
		// 返回用户信息
		UserSimpleInfoDto userSimpleInfoDto = new UserSimpleInfoDto();

		userSimpleInfoDto.setUserAccount(userAccount);
		userSimpleInfoDto.setAuthLevel(user.getAuthLevel());
		userSimpleInfoDto.setFullName(user.getFullname());
		userSimpleInfoDto.setNickName(user.getNickname());
		userSimpleInfoDto.setUid(user.getUid());
		userSimpleInfoDto.setBrokerId(user.getBrokerId());
		String token = tokenHelper.generateToken(user.getUid());
		userSimpleInfoDto.setToken(token);
		return userSimpleInfoDto;
	}

	// 手机号登录
	@RequestMapping(value = "/phone-login", method = RequestMethod.GET)

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginPasswordWithPhoneLoginStrategy'}})"))
	public UserSimpleInfoDto phoneLogin(@AuthForHeader AuthContext authContext) {
		User user = null;

		String userAccount = authContext.getUserAccount();

		user = userService.getUserLikePhone(userAccount);
		// update user表 锁定记录清空
		User userNew = new User();
		userNew.setUid(user.getUid());
		userNew.setLockNum((byte) 0);
		userNew.setUpdateDate(new Date());
		userService.updateByPrimaryKeySelective(userNew);

		// 登录成功之后记录用户登录ip与时间
		UserLoginLog userLoginLog = new UserLoginLog();
		userLoginLog.setUid(user.getUid());
		final String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {

			log.info("无法从用户uid:" + userLoginLog.getUid() + "请求header中的X-Real-IP获取真实IP地址");
			userLoginLog.setIpCountry("Unknow");
			userLoginLog.setIpCity("Unknow");
			userLoginLog.setIpAddress("Unknow");
			userService.recordUserLogin(userLoginLog);
		} else {
			CompletableFuture.runAsync(() -> {
				Map<String, String> map = null;
				try {
					map = getCountyAndCityByIPService.getCountyAndCityByIp(ip);
				} catch (Exception e) {
					userLoginLog.setIpCountry("Unknow");
					userLoginLog.setIpCity("Unknow");
				}
				if (null != map) {
					userLoginLog.setIpCountry(map.get("country"));
					userLoginLog.setIpCity(map.get("city"));
				}
				userLoginLog.setIpAddress(ip);
				userService.recordUserLogin(userLoginLog);
			});
		}
		// 返回用户信息
		UserSimpleInfoDto userSimpleInfoDto = new UserSimpleInfoDto();

		userSimpleInfoDto.setUserAccount(user.getMobile());
		userSimpleInfoDto.setAuthLevel(user.getAuthLevel());
		userSimpleInfoDto.setFullName(user.getFullname());
		userSimpleInfoDto.setNickName(user.getNickname());
		userSimpleInfoDto.setUid(user.getUid());
		userSimpleInfoDto.setBrokerId(user.getBrokerId());
		String token = tokenHelper.generateToken(user.getUid());
		userSimpleInfoDto.setToken(token);
		return userSimpleInfoDto;
	}

	// 登出
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public void loginout(@AuthForHeader AuthContext authContext) {
		authContext.setLoginSession(null);
	}

	@Strategys(strategys = {
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'},{'checkLoginStrategy','checkLoginPasswordStrategy','checkGoogleCodeStrategy'}})"), })
	@RequestMapping(value = "/login-password", method = RequestMethod.POST)
	public void changeloginPassword(@AuthForHeader AuthContext authContext, @RequestBody UserDto userDto) {

		String newPwd = userDto.getLoginPassword();

		User user = null;
		String account = authContext.getUserAccount();

		if (Strings.isNullOrEmpty(account)) {
			user = userService.getUserByUid(authContext.getLoginSession().getUserId());
			account = user.getEmail();
		} else {
			user = userService.getUserByEmail(account);
		}

		if (user == null) {
			throw new AppException(UserCodeConst.NO_REGISTER);
		}

		// 验证传入参数 与 处理参数
		if (StringUtils.isEmpty(newPwd)) {
			log.info("新登录密码为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (newPwd.contains(" ")) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}

		newPwd = ConstantUtil.charConvert(newPwd);
		// 登录成功：校验新密码格式；修改登录密码
		if (newPwd.length() < 6 || newPwd.length() > 20) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(newPwd)) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}

		userService.updatePassword(newPwd, user.getUid());
	}

	// 修改支付密码
	// "checkPayPasswordStregy",
	// "checkPassportStraegy"
	@RequestMapping(value = "/pay-password", method = RequestMethod.POST)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'},{'checkPassportStraegy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkGoogleCodeStrategy'}})") })
	public void changePayPassword(@RequestBody UserDto userDto, @AuthForHeader AuthContext authContext) {

		int uid = authContext.getLoginSession().getUserId();
		String newPayPwd = userDto.getPayPassword();
		// 验证传入参数 与 处理参数
		if (StringUtils.isEmpty(newPayPwd)) {
			log.info("新支付密码为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (newPayPwd.contains(" ")) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		newPayPwd = ConstantUtil.charConvert(newPayPwd);

		// 登录成功：校验新密码格式；修改登录密码

		if (newPayPwd.length() < 8 || newPayPwd.length() > 20) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(newPayPwd)) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}

		// user表 md5 insert操作
		userService.updatePayPassword(newPayPwd, uid);
	}


	// 手机修改支付密码
	@RequestMapping(value = "/phone-pay-password", method = RequestMethod.POST)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'},{'checkPassportStraegy'}})") })
	public void PhoneChangePayPassword(@RequestBody UserDto userDto, @AuthForHeader AuthContext authContext) {

		int uid = authContext.getLoginSession().getUserId();
		String mobile = userDto.getMobile();
		String newPayPwd = userDto.getPayPassword();
		newPayPwd = ConstantUtil.charConvert(newPayPwd);
		String code = userDto.getCode();

		// 登录成功：校验新密码格式；修改登录密码

		if (newPayPwd.length() < 8 || newPayPwd.length() > 20) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(newPayPwd)) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		//校验验证码
		String rediscode = redisTemplate.opsForValue().get("fund"+mobile+":code");
		if(!code.equals(rediscode)){
			throw new AppException(CommonCodeConst.CODE_ERROR);
		}
		// user表 md5 insert操作
		userService.updatePayPassword(newPayPwd, uid);
		redisTemplate.delete("fund"+mobile+":code");
		redisTemplate.delete("fund"+mobile+":avoid");
	}

	// 前端登录，提供输入账户后失焦查询该用户有无注册，及是否需要验证码及锁定次数

	@RequestMapping(value = "/need-captha", method = RequestMethod.GET)
	public void checkNeedCaptha(@RequestParam("userName") String userName) {

		User user = userFacade.getUser(userName);
		if (null == user) {
			user = userService.getUserLikePhone(userName);
			if(null == user){
				throw new AppException(UserCodeConst.NO_REGISTER);
			}
		}

		// 查询验证码锁定情况 小于3次正常返回；>=3次返回需要验证码code并返回相应次数
		CheckLoginLockedDto checkLoginLockedDto = userFacade.LoginPasswordLockNum(user.getUid());
		Integer lockedNum = checkLoginLockedDto.getLockedNum();

		// >=3次返回需要验证码code并返回相应次数
		if (lockedNum >= 3) {
			JSONObject json = new JSONObject();
			json.put("num", lockedNum);
			throw new AppException(SecurityCodeConst.NEED_VERIFICATION_CODE, "", json);

		}

	}

	@RequestMapping(value = "/freshen-token", method = RequestMethod.GET)
	public JSONObject freshentoken(@AuthForHeader AuthContext authContext) {
		String token = tokenHelper.refreshToken(authContext.getToke());
		JSONObject json = new JSONObject();
		json.put("token", token);
		return json;
	}

	// 用户绑定手机
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})") })
	@RequestMapping(value = "/cellphone-config", method = RequestMethod.GET)
	public void updatePhoneNumber(@AuthForHeader AuthContext authContext, @RequestParam("phone") String phoneNumber) {
		//		if (!authContext.getUserAccount().equals(phoneNumber)) {
		//			throw new AppException(IdentifyingCodeConst.IDENTIFYING_CODE_EEROR);
		//		}

		User user = userService.getUserLikePhone(phoneNumber);
		if (user != null) {

			throw new AppException(UserCodeConst.HAS_BINGDING);
		}
		Integer uid = authContext.getLoginSession().getUserId();
		userService.updateUserphoneNumber(phoneNumber, uid);
	}

	// 用户绑定邮箱
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/email-config", method = RequestMethod.GET)
	public void updateEmail(@AuthForHeader AuthContext authContext, @RequestParam("email") String email,@RequestParam("code") String code,@RequestParam("mobile") String mobile) {
		//校验验证码
		String rediscode = redisTemplate.opsForValue().get("setemail"+mobile+":code");
		if(!code.equals(rediscode)){
			throw new AppException(CommonCodeConst.CODE_ERROR);
		}
		redisTemplate.delete("setemail"+mobile+":code");
		redisTemplate.delete("setemail"+mobile+":avoid");

		User user = userService.getUserByEmail(email);
		if (user != null) {

			throw new AppException(UserCodeConst.EMAIL_HAS_BINGDING);
		}
		Integer uid = authContext.getLoginSession().getUserId();
		userService.updateUserEmail(email, uid);
	}

	// 用户设置昵称
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/nickname-config", method = RequestMethod.GET)
	public void updateNickName(@AuthForHeader AuthContext authContext, @RequestParam("nickName") String nickNmae) {

		Integer uid = authContext.getLoginSession().getUserId();
		userService.updateUserNickName(nickNmae, uid);

	}
	//设置支付密码
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkServiceCodeStrategy'}})")})
	@RequestMapping(value = "/paypassword-init", method = RequestMethod.POST)
	public void savePayPasssword(@AuthForHeader AuthContext authContext, @RequestBody UserDto userDto) {
		Integer uid = authContext.getLoginSession().getUserId();
		User user = userFacade.getUser(uid);
		if (!Strings.isNullOrEmpty(user.getPayPassword())) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_INIT_ERROR,"支付密码重复设置");
		}
		String payPwd = userDto.getPayPassword();
		// 验证传入参数 与 处理参数
		if (StringUtils.isEmpty(payPwd)) {
			log.info("新支付密码为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (payPwd.contains(" ")) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		payPwd = ConstantUtil.charConvert(payPwd);

		// 登录成功：校验新密码格式；修改登录密码

		if (payPwd.length() < 8 || payPwd.length() > 20) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(payPwd)) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}

		// user表 md5 insert操作
		userService.updatePayPassword(payPwd, uid);
	}

	//手机登录后设置支付密码
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/phone-paypassword-init", method = RequestMethod.POST)
	public void PhoneSavePayPasssword(@AuthForHeader AuthContext authContext, @RequestBody UserDto userDto) {
		Integer uid = authContext.getLoginSession().getUserId();
		String code = userDto.getCode();
		String mobile = userDto.getMobile();
		User user = userFacade.getUser(uid);
		if (!Strings.isNullOrEmpty(user.getPayPassword())) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_INIT_ERROR,"支付密码重复设置");
		}
		String payPwd = userDto.getPayPassword();
		// 验证传入参数 与 处理参数
		if (StringUtils.isEmpty(payPwd)) {
			log.info("新支付密码为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (payPwd.contains(" ")) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		payPwd = ConstantUtil.charConvert(payPwd);

		// 登录成功：校验新密码格式；修改登录密码

		if (payPwd.length() < 8 || payPwd.length() > 20) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(payPwd)) {
			throw new AppException(UserCodeConst.PAY_PASSWORD_VALID_ERROR);
		}
		//校验验证码
		String rediscode = redisTemplate.opsForValue().get("fund"+mobile+":code");
		if(!code.equals(rediscode)){
			throw new AppException(CommonCodeConst.CODE_ERROR);
		}
		redisTemplate.delete("fund"+mobile+":code");
		redisTemplate.delete("fund"+mobile+":avoid");
		// user表 md5 insert操作
		userService.updatePayPassword(payPwd, uid);
	}


	//手机号修改登录密码
	@RequestMapping(value = "/changephonepassword", method = RequestMethod.POST)
	public String changephoneloginPassword(@RequestBody UserDto userDto) {
		String newPwd = userDto.getLoginPassword();
		String mobile = userDto.getMobile();
		String code = userDto.getCode();
		User user = null;
		user = userService.getUserLikePhone(mobile);
		if (user == null) {
			throw new AppException(UserCodeConst.NO_REGISTER);
		}
		mobile= user.getMobile();
		// 验证传入参数 与 处理参数
		if (StringUtils.isEmpty(newPwd)) {
			log.info("新登录密码为null");
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		if (newPwd.contains(" ")) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		String rediscode = redisTemplate.opsForValue().get("cgphpassword"+mobile+":code");
		if(code.isEmpty()||!code.equals(rediscode)){
			log.info("验证码输入错误！");
			throw new AppException(CommonCodeConst.CODE_ERROR);
		}


		newPwd = ConstantUtil.charConvert(newPwd);
		// 登录成功：校验新密码格式；修改登录密码
		if (newPwd.length() < 6 || newPwd.length() > 20) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}
		if (!PasswordUtil.checkPasswordFormat(newPwd)) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_VALID_ERROR);
		}
		userService.updatePassword(newPwd, user.getUid());
		redisTemplate.delete("cgphpassword"+mobile+":code");
		redisTemplate.delete("cgphpassword"+mobile+":avoid");
		return UserCodeConst.SUCCESS;
	}

	//查询用户资金密码输入方式
	@RequestMapping(value = "/paypasswordInputTypeInfo", method = RequestMethod.GET)
	public UserPaypasswordRelationship paypasswordInputTypeInfo(@RequestParam("uid") Integer uid) {
		UserPaypasswordRelationship info = userPaypasswordRelatationService.selectByUid(uid);
		return info;
	}

	//更新用户资金密码输入方式
	@RequestMapping(value = "/updatePaypasswordInputType", method = RequestMethod.POST)
	public void updatePaypasswordInputType(@RequestBody UserPaypasswordRelationship userPaypasswordRelationship) {
		userPaypasswordRelationship.setUpdateDate(new Date());
		userPaypasswordRelatationService.updateByUid(userPaypasswordRelationship);
	}

	public String ipToCountry(String ip) throws GeoIp2Exception, Exception{
		File database = new File(serviceUrl+"/GeoLite2-City.mmdb");
		DatabaseReader reader = new DatabaseReader.Builder(database).build();
		InetAddress  ipAddress = InetAddress.getByName(ip);	  
		CityResponse  response = reader.city(ipAddress);
		Country country = response.getCountry(); 
		return country.getIsoCode();
	}

	//查询ip是否限制
	@RequestMapping(value = "/checkIp", method = RequestMethod.GET)
	public boolean checkIp() {
		String ip = request.getHeader("X-Real-IP");
		log.error("checkIp={}",ip);
		try {
			String countryCode = ipToCountry(ip);
			log.error("checkIp ip={},countryCode={}",ip,countryCode);
			if(countryCode.equals("CN")) {
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		
	}
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test() throws FileNotFoundException {
		String ip = request.getHeader("X-Real-IP");
		return ip;
	}
	
	@RequestMapping(value = "/check-pay-password", method = RequestMethod.GET)
	public void checkPayPassword(@AuthForHeader AuthContext authContext) {
		Integer userId = authContext.getLoginSession().getUserId();
	       
		String payPassword = authContext.getpayPassword();
		 
		// 校验支付密码正确与否
				if (StringUtils.isEmpty(payPassword)) {
					log.info("支付密码为null");
					throw new AppException(CommonCodeConst.FIELD_ERROR);
				}

				payPassword = ConstantUtil.charConvert(payPassword);

				User user = userService.getUserByUid(userId);
				if (user == null) {
					log.error("修改支付密码，在user表中找不到uid={} 的记录", userId);
					throw new AppException(CommonCodeConst.FIELD_ERROR);
				}
				CheckPayPasswordLockedDto checkPayPasswordLockedDto = userPayPasswordService.CheckPayPasswordLockedTimes(userId);

				// 验证支付密码
				if (!CryptoUtils.verify(user.getPayPassword(), MD5Util.genMD5Code(payPassword), user.getPaySalt())) {
					userPayPasswordService.addLockTimes(user.getUid());
					if (checkPayPasswordLockedDto.getLockedNum() >= 2) {
						throw new AppException(SecurityCodeConst.PAY_ACCOUNT_LOCK);
					}
					CheckPayPasswordLockedDto lockedTimes = userPayPasswordService.CheckPayPasswordLockedTimes(user.getUid());

					JSONObject json = new JSONObject();
					json.put("num", lockedTimes.getLockedNum());
					json.put("totalNum", 3);

					throw new AppException(UserCodeConst.PAY_PASSWORD_ERROR, "", json, lockedTimes.getLockedNum().toString());

				} else {
					userPayPasswordService.lockPayNumZero(userId);
				}
		
		
	}
}