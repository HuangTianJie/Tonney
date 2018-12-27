package com.gop.sms.chuanglan.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gop.domain.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gop.code.consts.IdentifyingCodeConst;
import com.gop.code.consts.MessageConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.common.CheckCodeService;
import com.gop.common.IdentifyingCodeService;
import com.gop.conetxt.EnvironmentContxt;
import com.gop.exception.AppException;
import com.gop.sms.chuanglan.interDemo.util.HttpUtil;
import com.gop.user.service.UserService;

@RestController
@RequestMapping("/intersmssend")
public class InterSmsSendController {

	private static final Logger logger = LogManager
			.getLogger(InterSmsSendController.class);

	@Value("${sms.url}")
	private  String smsurl;

	@Value("${sms.account}")
	private  String smsaccount;

	@Value("${sms.password}")
	private  String smspassword;
	
	@Autowired
	@Qualifier("userService")
	private UserService userService;

	@Autowired
	private CheckCodeService checkCodeService;
	
	@Autowired
	@Qualifier("IdentifyingCodeServiceImpl")
	IdentifyingCodeService identifyingCodeService;
	@Autowired
	private EnvironmentContxt environmentContxt;
	

	//手机注册发送短信验证码 
	@RequestMapping(value = "/sendsms", method = RequestMethod.GET)
	@ResponseBody
	public String sendsms(@RequestParam(value = "mobile") String mobile) {
		
		//校验手机号是否注册
		User userinfo = userService.getUserLikePhone(mobile);
		if(null != userinfo){
			throw new AppException(UserCodeConst.HAS_REGISTER);
		}

		String newmobile= mobile.replace("-", "");
		//验证码 value mobile:code
		String num = checkCodeService.SaveUserSendCode(mobile);
		
		// 短信内容。
		String msg = environmentContxt.getMsg(MessageConst.PHONE_MSG_INFO, num);

		String result = sendsms(newmobile,msg);

		logger.info("返回参数为:" + result);

		return result;

	}

	//手机重置密码发送短信验证码
	@RequestMapping(value = "/changepasswordsendsms", method = RequestMethod.GET)
	@ResponseBody
	public String changepasswordsendsms(@RequestParam(value = "mobile") String mobile) {
		User user = userService.getUserLikePhone(mobile);
		mobile = user.getMobile();
		String newmobile= mobile.replace("-", "");
		Boolean createCodeEnable = identifyingCodeService.checkSendCode("cgphpassword"+mobile);
		if (createCodeEnable) {
			throw new AppException(IdentifyingCodeConst.IDENTIFYING_CODE_SENDED);
		}
	

		String code = RandomStringUtils.randomNumeric(6);
		
		// 短信内容。
		String msg = environmentContxt.getMsg(MessageConst.UPDATE_LOGIN_CODE,code);
		identifyingCodeService.saveCode(code, "cgphpassword"+mobile, 60, 60);
		String result = sendsms(newmobile,msg);

		logger.info("返回参数为:" + result);

		return result;

	}
	
	//转出发送短信验证码
	@RequestMapping(value = "/rolloutsendsms", method = RequestMethod.GET)
	@ResponseBody
	public String rolloutsendsms(@RequestParam(value = "mobile") String mobile) {
		String newmobile= mobile.replace("-", "");
		Boolean createCodeEnable = identifyingCodeService.checkSendCode("rollout"+mobile);
		if (createCodeEnable) {
			throw new AppException(IdentifyingCodeConst.CODE_SENDED_MOBILE);
		}
	

		String code = RandomStringUtils.randomNumeric(6);
		
		// 短信内容。
		String msg = environmentContxt.getMsg(MessageConst.ROLL_OUT,code);
		identifyingCodeService.saveCode(code, "rollout"+mobile, 60, 60);
		String result = sendsms(newmobile,msg);

		logger.info("返回参数为:" + result);

		return result;

	}
	
	//资金密码设置发送短信验证码
	@RequestMapping(value = "/fundsendsms", method = RequestMethod.GET)
	@ResponseBody
	public String fundsendsms(@RequestParam(value = "mobile") String mobile) {
		String newmobile= mobile.replace("-", "");
		Boolean createCodeEnable = identifyingCodeService.checkSendCode("fund"+mobile);
		if (createCodeEnable) {
			throw new AppException(IdentifyingCodeConst.IDENTIFYING_CODE_SENDED);
		}
	

		String code = RandomStringUtils.randomNumeric(6);
		
		// 短信内容。
		String msg = environmentContxt.getMsg(MessageConst.FUND,code);
		identifyingCodeService.saveCode(code, "fund"+mobile, 60, 60);
		String result = sendsms(newmobile,msg);

		logger.info("返回参数为:" + result);

		return result;

	}
	
	//设置邮箱发送短信验证码
	@RequestMapping(value = "/setemailsendsms", method = RequestMethod.GET)
	@ResponseBody
	public String setemailsendsms(@RequestParam(value = "mobile") String mobile) {
		String newmobile= mobile.replace("-", "");
		Boolean createCodeEnable = identifyingCodeService.checkSendCode("setemail"+mobile);
		if (createCodeEnable) {
			throw new AppException(IdentifyingCodeConst.IDENTIFYING_CODE_SENDED);
		}
	

		String code = RandomStringUtils.randomNumeric(6);
		
		// 短信内容。
		String msg = environmentContxt.getMsg(MessageConst.SETEMAIL,code);
		identifyingCodeService.saveCode(code, "setemail"+mobile, 60, 60);
		String result = sendsms(newmobile,msg);

		logger.info("返回参数为:" + result);

		return result;

	}


	public String sendsms(String mobile,String msg) {

		// 请求地址
		String url = smsurl;

		// API账号，50位以内。必填
		String account = smsaccount;

		// API账号对应密钥，联系客服获取。必填
		String password = smspassword;

		// 组装请求参数
		JSONObject map = new JSONObject();
		map.put("account", account);
		map.put("password", password);
		map.put("msg", msg);
		map.put("mobile", mobile);

		String params = map.toString();

		logger.info("请求参数为:" + params);
		try {
			String result = HttpUtil.post(url, params);

			logger.info("返回参数为:" + result);

			JSONObject jsonObject = JSON.parseObject(result);
			String code = jsonObject.get("code").toString();
			String msgid = jsonObject.get("msgid").toString();
			String error = jsonObject.get("error").toString();
			logger.info("状态码:" + code + ",状态码说明:" + error + ",消息id:" + msgid);
			return code;
		} catch (Exception e) {
			logger.error("请求异常：" + e);
			return e.toString();
		}

	}
}
