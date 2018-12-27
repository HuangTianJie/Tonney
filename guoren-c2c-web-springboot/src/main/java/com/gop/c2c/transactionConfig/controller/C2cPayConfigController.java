package com.gop.c2c.transactionConfig.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.gop.authentication.service.UserResourceManagerService;
import com.gop.c2c.dto.C2cAliPayDto;
import com.gop.c2c.dto.C2cAlipayInfoDto;
import com.gop.c2c.dto.C2cBankDto;
import com.gop.c2c.dto.C2cBankInfoDto;
import com.gop.c2c.dto.C2cBasePayChannelDto;
import com.gop.c2c.dto.C2cWechatpayInfoDto;
import com.gop.c2c.service.C2cAlipayInfoService;
import com.gop.c2c.service.C2cBankInfoService;
import com.gop.c2c.service.C2cWechatService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.domain.C2cAlipayInfo;
import com.gop.domain.C2cBankInfo;
import com.gop.domain.C2cWeChatInfo;
import com.gop.domain.User;
import com.gop.domain.UserUploadResourceLog;
import com.gop.domain.enums.C2cPayAccountStatus;
import com.gop.domain.enums.C2cPayType;
import com.gop.exception.AppException;
import com.gop.uploadLog.UserUploadResourcLogService;
import com.gop.user.dto.CheckPayPasswordLockedDto;
import com.gop.user.service.UserPayPasswordService;
import com.gop.user.service.UserService;
import com.gop.util.ConstantUtil;
import com.gop.util.CryptoUtils;
import com.gop.util.MD5Util;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import lombok.extern.slf4j.Slf4j;

@RestController("C2cPayConfigController")
@RequestMapping("/payconfig")
@Slf4j
/**
 * 
 * @author zhangliwei
 *
 */
public class C2cPayConfigController {
	@Autowired
	@Qualifier("C2cBankInfoService")
	private C2cBankInfoService c2cBankInfoService;

	@Autowired
	@Qualifier("C2cAlipayInfoService")
	private C2cAlipayInfoService c2cAlipayInfoService;

	@Autowired
	@Qualifier("C2cWechatService")
	private C2cWechatService c2cWechatService;

	@Autowired
	@Qualifier("MongoManagerServiceImpl")
	private UserResourceManagerService userResourceManagerServicemongo;

	@Autowired
	@Qualifier("UserUploadResourcLogServiceImpl")
	private UserUploadResourcLogService uploadLogService;

	@Autowired
	private UserPayPasswordService userPayPasswordService;

	@Autowired
	private UserService userService;

	//添加阿里支付信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	@RequestMapping(value = "/c2calipay-config", method = RequestMethod.POST)
	public void addC2cAlipayInfo(@AuthForHeader AuthContext authContext,@RequestBody C2cAliPayDto c2cAlipayInfoDto) {

		C2cAlipayInfo c2cAlipayInfo = new C2cAlipayInfo();
		c2cAlipayInfo.setAlipayNo(c2cAlipayInfoDto.getAlipayNo());
		c2cAlipayInfo.setName(c2cAlipayInfoDto.getName());
		c2cAlipayInfo.setStatus(C2cPayAccountStatus.USING);
		Integer uid = authContext.getLoginSession().getUserId();
		c2cAlipayInfo.setUid(uid);
		c2cAlipayInfoService.addC2cUserAlipay(c2cAlipayInfo,uid);	
	}

	//添加银行支付信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	@RequestMapping(value = "/c2cbank-config", method = RequestMethod.POST)
	public void addC2cBankInfo(@AuthForHeader AuthContext authContext,@RequestBody C2cBankDto c2cBankInfoDto) {

		C2cBankInfo c2cBankInfo = new C2cBankInfo();
		c2cBankInfo.setAcnumber(c2cBankInfoDto.getAcnumber());
		c2cBankInfo.setBank(c2cBankInfoDto.getBank());
		c2cBankInfo.setName(c2cBankInfoDto.getName());
		c2cBankInfo.setSubbank(c2cBankInfoDto.getSubBank());
		c2cBankInfo.setStatus(C2cPayAccountStatus.USING);
		Integer uid = authContext.getLoginSession().getUserId();
		c2cBankInfo.setUid(uid);
		c2cBankInfoService.addC2cUserBank(c2cBankInfo, uid);			
	}

	//查询用户的支付渠道信息
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/c2cinfo-query", method = RequestMethod.GET)
	public List<C2cBasePayChannelDto> addC2cBankInfo(@AuthForHeader AuthContext authContext) {

		Integer uid = authContext.getLoginSession().getUserId();
		C2cBankInfo c2cBankInfo = c2cBankInfoService.selectByUid(uid);
		C2cAlipayInfo c2cAlipayInfo = c2cAlipayInfoService.selectByUid(uid);
		C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(uid);
		if(info==null) {
			info = new C2cWeChatInfo();
		}
		C2cAlipayInfoDto c2cAlipayInfoDto = C2cAlipayInfoDto.builder().uid(c2cAlipayInfo.getUid()).alipayNo(c2cAlipayInfo.getAlipayNo()).name(c2cAlipayInfo.getName()).createDate(c2cAlipayInfo.getCreateDate()).updateDate(c2cAlipayInfo.getUpdateDate()).build();
		c2cAlipayInfoDto.setC2cPayType(C2cPayType.ALIPAY);
		C2cBankInfoDto c2cBankInfoDto = C2cBankInfoDto.builder().bank(c2cBankInfo.getBank()).subBank(c2cBankInfo.getSubbank()).acnumber(c2cBankInfo.getAcnumber()).name(c2cBankInfo.getName()).build();
		c2cBankInfoDto.setC2cPayType(C2cPayType.BANK);
		User userinfo = userService.getUserByUid(uid);
		C2cWechatpayInfoDto c2cWechatInfoDto = C2cWechatpayInfoDto.builder().tag(info.getTag()).uid(info.getUid()).name(userinfo.getFullname()).build();
		c2cWechatInfoDto.setC2cPayType(C2cPayType.WECHAT);
		List<C2cBasePayChannelDto> c2cBasePayChannelDtos = Lists.newArrayList();
		c2cBasePayChannelDtos.add(c2cAlipayInfoDto);
		c2cBasePayChannelDtos.add(c2cBankInfoDto);
		c2cBasePayChannelDtos.add(c2cWechatInfoDto);
		return c2cBasePayChannelDtos;			
	}

	//添加微信支付，上传图片

	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/upload-wechat-pic/{imageTag}", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadPhoto(@AuthForHeader AuthContext context, @RequestParam("file") MultipartFile file,
			@PathVariable(value = "imageTag", required = false) String imageTag){
		try {
			File mfile = null ;
			MultiFormatReader formatReader = new MultiFormatReader();
			BufferedImage image = ImageIO.read(file.getInputStream());
			BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

			// 定义二维码的参数
			HashMap hints = new HashMap();
			//设置编码字符集
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

			//处理读取结果
			Result result = formatReader.decode(binaryBitmap, hints);
			log.error("处理读取结果={}",result.toString());
			if(!result.toString().contains("wxp:")) {
				throw new AppException(CommonCodeConst.WECHAT_PIC_ERROR);
			}
		}catch (Exception e) {
			throw new AppException(CommonCodeConst.WECHAT_PIC_ERROR);
		}


		if (Strings.isNullOrEmpty(imageTag)) {
			imageTag = "public";
		}
		Integer uid = context.getLoginSession().getUserId();

		log.info(file.getContentType());

		if (file == null || file.isEmpty()) {
			log.error("上传文件为空！");
			throw new AppException(CommonCodeConst.FIELD_ERROR, null);
		}
		String tag = null;

		try {
			log.error("上传文件不为空");
			tag = userResourceManagerServicemongo.saveResourcesWithPrivate(file.getInputStream());
			log.error("上传文件tag={}",tag);
			//新增上传日志
			UserUploadResourceLog log = new UserUploadResourceLog();
			log.setUid(uid);
			log.setTag(tag);
			log.setDatatype("string");
			log.setSoucre(imageTag);
			log.setCreatetime(new Date());
			log.setUpdatetime(new Date());
			log.setStoretype("qiniu");
			uploadLogService.loggingUserUpload(log);
			//新增微信支付
			C2cWeChatInfo param = new C2cWeChatInfo();
			param.setUid(uid);
			param.setTag(tag);
			param.setStatus(C2cPayAccountStatus.USING);
			c2cWechatService.addC2cWechat(param);
		} catch (IOException e) {
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		if (tag == null) {
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		JSONObject json = new JSONObject();
		json.put("name", tag);
		// 返回json字符串给前端,dto等以后再改
		return json;
	}

	//查询微信支付图片name
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/wechat_pic_name", method = RequestMethod.GET)
	public String getPhoto(HttpServletResponse resp,@AuthForHeader AuthContext authContext) {
		String name= "";
		Integer uid = authContext.getLoginSession().getUserId();
			C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(uid);
			if(info==null) {
				return name;
			}
			name = info.getTag();
			return name;
	}

	//校验支付密码是否正确
	@RequestMapping(value = "/check-paypassword", method = RequestMethod.POST)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	public void checkPayPassword(@AuthForHeader AuthContext authContext) {}

}
