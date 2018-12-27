package com.gop.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.gop.authentication.service.UserResourceManagerService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.common.Environment.EnvironmentEnum;
import com.gop.conetxt.EnvironmentContxt;
import com.gop.domain.Advetisement;
import com.gop.domain.UserUploadResourceLog;
import com.gop.exception.AppException;
import com.gop.uploadLog.UserUploadResourcLogService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/common")
@Slf4j
public class CommonController {

	public static JSONObject json;

	public static ScheduledExecutorService sche = Executors.newScheduledThreadPool(10);

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Description:获取图片
	 * 
	 * @return 返回状态
	 * @see
	 */
	// 七牛的实现
	@Autowired
	@Qualifier("ImageQiniuServiceImpl")
	private UserResourceManagerService userResourceManagerServiceqiNiu;

	@Autowired
	@Qualifier("MongoManagerServiceImpl")
	private UserResourceManagerService userResourceManagerServicemongo;

	@Autowired
	@Qualifier("UserUploadResourcLogServiceImpl")
	private UserUploadResourcLogService uploadLogService;
	@Autowired
	private EnvironmentContxt environmentContxt;
	
	@Value(value="${service.ipprot}")
	private String ipport;

	@PostConstruct
	public void init() {
//Marked by hoff 20180919
		sche.scheduleWithFixedDelay(() -> {
			try {
//				json = restTemplate.getForEntity("https://api.bitfinex.com/v1/pubticker/BTCUSD", JSONObject.class)
//						.getBody();
				String btcStr = "{\"mid\":\"6288.0\",\"bid\":\"6287.8\",\"ask\":\"6288.2\",\"last_price\":\"6288.7\",\"low\":\"6252.0\",\"high\":\"6639.0\",\"volume\":\"36372.27027844\",\"timestamp\":\"1539243275.2037592\"}"; 
				json = JSONObject.parseObject(btcStr);
			} catch (RestClientException e) {
				log.error("查询usdbtc异常", e);
			}
		}, 0, 10, TimeUnit.MINUTES);
//end of marking
	}

	@PreDestroy
	public void destory() {
		if (!sche.isShutdown()) {
			sche.shutdownNow();
		}

	}

	/**
	 * 获取图片
	 * 
	 * @param name
	 * @param resp
	 */

	@RequestMapping(value = "/photo", method = RequestMethod.GET)
	public void getPhoto(@RequestParam("name") String name, HttpServletResponse resp,
			@AuthForHeader AuthContext context) {
		JSONObject json = new JSONObject();
		InputStream imageStream = null;
		if (environmentContxt.getSystemEnvironMent().equals(EnvironmentEnum.CHINA)) {
			imageStream = userResourceManagerServiceqiNiu.getResourcesWithPrivateStream(name);
		} else {
			imageStream = userResourceManagerServicemongo.getResourcesWithPrivateStream(name);
		}

		if (imageStream == null) {
			try {
				resp.getOutputStream().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// json.put("base64", resources);
		// return json;
		try {

			byte[] buffer = new byte[1024];
			// BufferedInputStream b = new BufferedInputStream(imageStream);
			int len = -1;
			while ((len = imageStream.read(buffer)) != -1) {
				resp.getOutputStream().write(buffer, 0, len);
			}
		} catch (Exception e) {
			// TODO: handle exception

		} finally {
			try {
				resp.getOutputStream().close();
				if (imageStream != null) {

					imageStream.close();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// return imageStream;

	}

	/**
	 * 上传图片
	 * 
	 * @param context
	 * @param file
	 * @param imageTag
	 *            图片参数,区分七牛存储空间
	 * @return
	 */
	@Strategys(strategys = @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"))
	@RequestMapping(value = "/upload-photo/{imageTag}", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadPhoto(@AuthForHeader AuthContext context, @RequestParam("file") MultipartFile file,
			@PathVariable(value = "imageTag", required = false) String imageTag) {
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
			if ("private".equals(imageTag)) {
				if (environmentContxt.getSystemEnvironMent().equals(EnvironmentEnum.CHINA)) {
					tag = userResourceManagerServiceqiNiu.saveResourcesWithPrivate(file.getInputStream());
				} else {
					tag = userResourceManagerServicemongo.saveResourcesWithPrivate(file.getInputStream());
				}
			} else if ("public".equals(imageTag)) {
				if (environmentContxt.getSystemEnvironMent().equals(EnvironmentEnum.CHINA)) {
					tag = userResourceManagerServiceqiNiu.saveResourcesWithPublic(file.getInputStream());
				} else {
					tag = userResourceManagerServicemongo.saveResourcesWithPublic(file.getInputStream());
				}
			}

			UserUploadResourceLog log = new UserUploadResourceLog();
			log.setUid(uid);
			log.setTag(tag);
			log.setDatatype("string");
			log.setSoucre(imageTag);
			log.setCreatetime(new Date());
			log.setUpdatetime(new Date());
			log.setStoretype("qiniu");
			uploadLogService.loggingUserUpload(log);

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

	@RequestMapping(value = "/usdbtc", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getBit() {
		return json;

	}
	
	@RequestMapping(value = "/pic", method = RequestMethod.GET)
	@ResponseBody
	public List<Advetisement> pic() {
		List<Advetisement> lists = new ArrayList<Advetisement>();
		Advetisement info = new Advetisement();
		info.setPicurl(ipport+"img/banner2018-05-30_b684c0.png");
		info.setStepurl("http://www.baidu.com");
		lists.add(info);
		
		Advetisement info2 = new Advetisement();
		info2.setPicurl(ipport+"img/banner2018-05-30_b684c0.png");
		info2.setStepurl("http://www.baidu.com");
		lists.add(info2);
		
		Advetisement info3 = new Advetisement();
		info3.setPicurl(ipport+"img/banner2018-05-30_b684c0.png");
		info3.setStepurl("http://www.baidu.com");
		lists.add(info3);
		return lists;
	}

}
