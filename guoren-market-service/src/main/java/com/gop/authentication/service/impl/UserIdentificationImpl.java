package com.gop.authentication.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.authentication.service.UserBasicInfoService;
import com.gop.authentication.service.UserIdentificationService;
import com.gop.authentication.service.UserResourceManagerService;
import com.gop.code.consts.AuditCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.domain.HttpsUtil;
import com.gop.domain.Response;
import com.gop.domain.User;
import com.gop.domain.UserBasicInfo;
import com.gop.domain.UserIdentification;
import com.gop.domain.UserUploadResourceLog;
import com.gop.domain.enums.AuditDealStatus;
import com.gop.domain.enums.AuditStatus;
import com.gop.domain.enums.AuditFirst;
import com.gop.domain.enums.AuthLevel;
import com.gop.domain.enums.Gender;
import com.gop.exception.AppException;
import com.gop.mapper.UserIdentificationMapper;
import com.gop.mode.vo.PageModel;
import com.gop.uploadLog.UserUploadResourcLogService;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import sun.misc.BASE64Decoder;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserIdentificationImpl implements UserIdentificationService {

	@Autowired
	private UserIdentificationMapper userIdentificationMapper;

	@Autowired
	private UserBasicInfoService userBasicInfoService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	@Qualifier("stringRedisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private UserIdentificationService userIdentificationService;

	@Value(value="${get.face.token.url}")
	private String getFaceTokenUrl;

	@Value(value="${access.identity.url}")
	private String accessIdentityUrl;

	@Value(value="${get.result.url}")
	private String getResultUrl;

	@Value(value="${api.key}")
	private String appKey;

	@Value(value="${app.secret}")
	private String appSecret;

	@Value(value="${return.url}")
	private String returnUrl;

	@Value(value="${notify.url}")
	private String notifyUrl;

	@Value(value="${biz.no}")
	private String bizNo;

	@Value(value="${idcard.mode}")
	private String idcardMode;

	@Value(value="${comparison.type}")
	private String comparisonType;

	@Autowired
	@Qualifier("MongoManagerServiceImpl")
	private UserResourceManagerService userResourceManagerServicemongo;

	@Autowired
	@Qualifier("UserUploadResourcLogServiceImpl")
	private UserUploadResourcLogService uploadLogService;

	@Transactional
	public void insertUserIdentificationAndUserBasicInfo(UserBasicInfo userBasicInfo,
			UserIdentification userIdentification) {
		// 保存基本信息
		userBasicInfoService.insertOrUpdate(userBasicInfo);
		// 查询身份认证记录
		UserIdentification identityInfo = userIdentificationMapper.getLastIdentityInfoByUid(userBasicInfo.getUid());
		// 校验验证状态
		if (null != identityInfo && !AuditStatus.FAIL.equals(identityInfo.getAuditStatus())) {
			// log.error("用户有待审核或者已完成审核的状态");
			throw new AppException(AuditCodeConst.UNVERIFY_EXIST, null);
		}
		// 校验是否首次
		if (identityInfo != null) {
			userIdentification.setAuditFirst(AuditFirst.NO);
		} else {
			userIdentification.setAuditFirst(AuditFirst.YES);
		}

		// 设置auditStatus为待审核
		userIdentification.setAuditStatus(AuditStatus.INIT);
		//
		userIdentification.setStatus(AuditDealStatus.INIT);
		userIdentificationMapper.insert(userIdentification);
	}

	@Override
	public UserIdentification getserIdentificationById(Integer id) {
		try {
			UserIdentification identityInfoById = userIdentificationMapper.getIdentityInfoById(id);
			return identityInfoById;
		} catch (Exception e) {
			// log.info("根据id查询用户身份认证信息失败");
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

	}

	@Override
	public PageModel<UserIdentification> getUserIdentificationPageModel(Integer uid, AuditDealStatus status,
			Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		PageModel<UserIdentification> pageModel = new PageModel<>();
		try {
			PageInfo<UserIdentification> pageInfo = new PageInfo<>(
					userIdentificationMapper.getIdentityInfoList(uid, status));
			pageModel.setPageNo(pageNo);
			pageModel.setPageNum(pageInfo.getPageNum());
			pageModel.setPageNum(pageInfo.getPages());
			pageModel.setTotal(pageInfo.getTotal());
			// pageModel.setList(pageInfo.getList());
			pageModel.setList(pageInfo.getList().stream().map(r -> {
				User user = userFacade.getUser(null != uid ? uid : r.getUid());
				String email = null != user ? user.getEmail() : "";
				String mobile = null != user ? user.getMobile() : "";
				r.setEmail(email);
				r.setMobile(mobile);
				return r;
			}).collect(Collectors.toList()));
		} catch (Exception e) {
			// log.error("用户身份认证审核页面查询异常", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		return pageModel;
	}

	@Transactional
	public void updateUserIdentificationAndBasicInfo(UserBasicInfo basicInfo,
			UserIdentification getserIdentificationById) {
		try {
			userBasicInfoService.insertOrUpdate(basicInfo);
			userIdentificationMapper.updateAudit(getserIdentificationById);
		} catch (Exception e) {
			// log.error("更新用户审核level0-level1失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public void updateUserIdentification(UserIdentification getserIdentificationById) {
		try {
			userIdentificationMapper.updateAudit(getserIdentificationById);
		} catch (Exception e) {
			// log.error("更新用户审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public UserIdentification getLastUserIdentificationInfo(Integer uid) {
		UserIdentification userIdentification = userIdentificationMapper.getLastUserIdentificationInfoByUid(uid);
		return userIdentification;
	}

	@Override
	public List<UserIdentification> getLastUserIdentificationInfoList(Integer uid) {
		try {
			List<UserIdentification> identityHistoryList = userIdentificationMapper.getIdentityHistoryList(uid);
			return identityHistoryList;
		} catch (Exception e) {
			// log.error("查询用户身份认证历史审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public List<UserIdentification> getIdentityHistoryListByLimitNo(Integer uid, Integer limitNo) {
		try {
			List<UserIdentification> identityHistoryList = userIdentificationMapper.getIdentityHistoryListByLimitNo(uid,
					limitNo);
			return identityHistoryList;
		} catch (Exception e) {
			// log.error("查询用户身份限定数量的认证历史审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public UserIdentification getUserIdentificationInfoWtihAuditStatus(Integer uid, AuditStatus status) {
		try {
			UserIdentification identification = userIdentificationMapper.getUserIdentificationInfoWtihAuditStatus(uid,
					status);
			return identification;
		} catch (Exception e) {
			// log.error("查询用户身份限定数量的认证历史审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public Integer getAmountOfIdentificationWithStatus(AuditStatus status) {
		try {
			return userIdentificationMapper.getAmountOfIdentificationWithStatus(status);
		} catch (Exception e) {
			// log.error("查询限定状态的认证历史审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public UserIdentification getUserByUidAndStatusAndAuditStatus(Integer uid, AuditDealStatus status,
			AuditStatus auditStatus) {
		try {
			return userIdentificationMapper.getUserByUidAndStatusAndAuditStatus(uid, status, auditStatus);
		} catch (Exception e) {
			// log.error("查询限定状态的认证历史审核信息失败", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public int countUserLevel(Date date) {
		return userIdentificationMapper.countUserLevel(date);
	}

	@Override
	public Response<String> territory(Integer uid) {
		Response<String> resp=new Response<String>();
		Map<String, String> map=new HashMap<String, String>();
		map.put("api_key", appKey);
		map.put("api_secret", appSecret);
		map.put("return_url", returnUrl+"?uid="+uid);
		map.put("notify_url", notifyUrl);
		map.put("biz_no", bizNo);
		map.put("idcard_mode", idcardMode);
		map.put("comparison_type", comparisonType);

		try {
			String result= HttpsUtil.formUpload(getFaceTokenUrl, map,"POST");
			JSONObject json=JSONObject.parseObject(result);
			if (null!=result && !"".equals(result) && null!=json.get("token")) {
				String token=json.get("token").toString();
				String user_id = uid.toString();
				String biz_id=json.get("biz_id").toString();
				redisTemplate.opsForValue().set("biz_id".concat(user_id), biz_id);
				String url=accessIdentityUrl+"?token="+token;
				resp.SetResponseDetail(url);
			}
		} catch (Exception e) {
			log.error("人脸识别接口请求异常",e);
			resp.setStatus("9999");
		}

		return resp;
	}

	@Override
	public Response<String> faceCallBack(String user_id){
		Response<String> resp=new Response<String>();
		String biz_id = redisTemplate.opsForValue().get("biz_id".concat(user_id));
		try {
			String param="?api_key="+appKey+"&api_secret="+appSecret+"&biz_id="+biz_id+"&return_image=4";
			String result= HttpsUtil.httpGet(getResultUrl+param);
			log.info("face返回结果："+result);
			JSONObject json=JSONObject.parseObject(result);
			if (null!=result && !"".equals(result) && null!=json.get("status")) {
				String status=json.get("status").toString();
				if("OK".equals(status)){
					//获取人脸识别信息
					Map idCardInfo=JSONObject.parseObject(JSONObject.toJSONString(json.get("idcard_info")),Map.class);
					Object idcard_number=idCardInfo.get("idcard_number");
					Object idcard_name=idCardInfo.get("idcard_name");
					//获得认证 生日，户口地址
					Map front_side=JSONObject.parseObject(JSONObject.toJSONString(idCardInfo.get("front_side")),Map.class);
					Map ocr_result=JSONObject.parseObject(JSONObject.toJSONString(front_side.get("ocr_result")),Map.class);
					Object gender=ocr_result.get("gender");
					//身份证有效期
					Map back_side=JSONObject.parseObject(JSONObject.toJSONString(idCardInfo.get("back_side")),Map.class);
					Map ocr_result_to=JSONObject.parseObject(JSONObject.toJSONString(back_side.get("ocr_result")),Map.class);
					String validDate=ocr_result_to.get("valid_date")+"";

					//获得生日年月日
					Map birthday=JSONObject.parseObject(JSONObject.toJSONString(ocr_result.get("birthday")),Map.class);
					String day=birthday.get("day")+"";
					String month=birthday.get("month")+"";
					String year=birthday.get("year")+"";
					//获得实名认证阈值
					Map verify_result=JSONObject.parseObject(JSONObject.toJSONString(json.get("verify_result")),Map.class);
					Map result_faceid=JSONObject.parseObject(JSONObject.toJSONString(verify_result.get("result_faceid")),Map.class);
					BigDecimal confidence=(BigDecimal)result_faceid.get("confidence");
					Map thresholds=JSONObject.parseObject(JSONObject.toJSONString(result_faceid.get("thresholds")),Map.class);
					BigDecimal le_5=(BigDecimal)thresholds.get("1e-5");
					//获得身份证图片
					Map images=JSONObject.parseObject(JSONObject.toJSONString(json.get("images")),Map.class);
					String image_idcard_front=images.get("image_idcard_front")+"";
					String image_idcard_back=images.get("image_idcard_back")+"";
					String image_best=images.get("image_best")+"";
					int image_idcard_front_num = image_idcard_front.indexOf(",");
					image_idcard_front = image_idcard_front.substring(image_idcard_front_num+1);


					BASE64Decoder decoder = new BASE64Decoder();
					byte[] b = decoder.decodeBuffer(image_idcard_front);
					InputStream frontinputStreambs = new ByteArrayInputStream(b);
					String image_idcard_front_tag = userResourceManagerServicemongo.saveResourcesWithPrivate(frontinputStreambs);
					UserUploadResourceLog log = new UserUploadResourceLog();
					log.setUid(Integer.valueOf(user_id));
					log.setTag(image_idcard_front_tag);
					log.setDatatype("string");
					log.setSoucre("private");
					log.setCreatetime(new Date());
					log.setUpdatetime(new Date());
					log.setStoretype("qiniu");
					uploadLogService.loggingUserUpload(log);
					
					byte[] back = decoder.decodeBuffer(image_idcard_back);
					InputStream backinputStreambs = new ByteArrayInputStream(back);
					String image_idcard_back_tag = userResourceManagerServicemongo.saveResourcesWithPrivate(backinputStreambs);
					UserUploadResourceLog backlog = new UserUploadResourceLog();
					backlog.setUid(Integer.valueOf(user_id));
					backlog.setTag(image_idcard_back_tag);
					backlog.setDatatype("string");
					backlog.setSoucre("private");
					backlog.setCreatetime(new Date());
					backlog.setUpdatetime(new Date());
					backlog.setStoretype("qiniu");
					uploadLogService.loggingUserUpload(backlog);
					
					


					int image_best_num = image_best.indexOf(",");
					image_best = image_best.substring(image_best_num+1);

					byte[] c = decoder.decodeBuffer(image_best);
					InputStream bestinputStreambs = new ByteArrayInputStream(c);
					String image_best_tag = userResourceManagerServicemongo.saveResourcesWithPrivate(bestinputStreambs);
					UserUploadResourceLog logs = new UserUploadResourceLog();
					logs.setUid(Integer.valueOf(user_id));
					logs.setTag(image_best_tag);
					logs.setDatatype("string");
					logs.setSoucre("private");
					logs.setCreatetime(new Date());
					logs.setUpdatetime(new Date());
					logs.setStoretype("qiniu");
					uploadLogService.loggingUserUpload(log);

					//新增初始数据
					UserBasicInfo userBasicInfo = new UserBasicInfo();
					userBasicInfo.setBirthday(year+"-"+month+"-"+day);
					userBasicInfo.setCountry("中国");
					userBasicInfo.setCountryId("CN");
					userBasicInfo.setFirstName(idcard_name.toString());
					userBasicInfo.setMiddleName("");
					userBasicInfo.setLastName("");
					if(gender.toString().equals("女")){
						userBasicInfo.setGender(Gender.FEMALE);
					}else{
						userBasicInfo.setGender(Gender.MALE);
					}
					userBasicInfo.setUid(Integer.valueOf(user_id));

					// dto提取userIdentification
					UserIdentification userIdentification = new UserIdentification();
					userIdentification.setUid(Integer.valueOf(user_id));
					userIdentification.setCountryId("CN");
					userIdentification.setCountry("中国");
					userIdentification.setCardType("ID_CARD");
					userIdentification.setCardNo(idcard_number.toString());

					validDate = validDate.substring(validDate.indexOf("-")+1);
					validDate = validDate.replace(".", "-");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					userIdentification.setExpiredDate(sdf.parse(validDate));
					userIdentification.setCardPhoto("["+"\""+image_idcard_front_tag+"\""+","+"\""+image_idcard_back_tag+"\""+"]");
					userIdentification.setCardHandhold(image_best_tag);
					userIdentification.setCardTranslate("");
					// 因为数据库不能为null,所以此处设置为0
					userIdentification.setAuditUid(0);
					userIdentification.setAuditMessageId("0");
					userIdentification.setAuditMessage("");
					userIdentification.setFullName(
							userBasicInfo.getFirstName() + " " + userBasicInfo.getMiddleName() + " " + userBasicInfo.getLastName());

					userIdentificationService.insertUserIdentificationAndUserBasicInfo(userBasicInfo, userIdentification);

					UserIdentification userIdentificationInfo = userIdentificationService.getLastUserIdentificationInfo(Integer.valueOf(user_id));
					//比对结果的置信度 大于 误识率为十万分之一的置信度阈值 则为同一个人，实名认证通过
					if (confidence.compareTo(le_5) >= 0) {
						//认证成功
						// 数据库必须存在并且为init待审核状态
						if (null == userIdentificationInfo || !AuditStatus.INIT.equals(userIdentificationInfo.getAuditStatus())) {
							//							log.error("user_identification表不存在id为{}的记录!", id);
							throw new AppException(CommonCodeConst.FIELD_ERROR, null);
						}
						// 不能已认证过
						if (AuditDealStatus.FINISH.equals(userIdentificationInfo.getStatus())) {
							//							log.info("用户认证等级核对重复认证");
							throw new AppException(CommonCodeConst.FIELD_ERROR, null);
						}
						// 状态必须是0
						User userByUid = userService.getUserByUid(Integer.valueOf(user_id));
						if (!AuthLevel.LEVEL0.equals(userByUid.getAuthLevel())) {
							//							log.error("用户认证等级核对异常,应为level0");
							throw new AppException(UserCodeConst.CERTIFICATION_NO_MATCH);
						}
						// 根据认证状态更新用户相应信息(待审核状态变换:status与auditstatus,是否完成,审核意见内容,用户表中的全名,等级认证等)
						// 更新身份认证审核
						// 除了基本信息以外的两个认证是有历史记录的,默认即便此次认证失败,但是基础信息已被更新
						// 此处更新对象使用查询更新,不使用new对象
						userIdentificationInfo.setAuditStatus(AuditStatus.OK);
						userIdentificationInfo.setAuditDate(new Date());
						// 判断审核结果,如果通过则将status设为finish(数据库中status与auditstatus的init容易歧义,应注意)
						userIdentificationInfo.setStatus(AuditDealStatus.FINISH);
						// 更新用户认证
						userIdentificationService.updateUserIdentification(userIdentificationInfo);

						// 升级用户等级到level1
						userFacade.updateAuthStatus(Integer.valueOf(user_id), AuthLevel.LEVEL1);
					}else{
						//认证失败
						//更新审核状态
						userIdentificationInfo.setAuditStatus(AuditStatus.FAIL);
						userIdentificationInfo.setAuditDate(new Date());
						// 判断审核结果,如果通过则将status设为finish(数据库中status与auditstatus的init容易歧义,应注意)
						userIdentificationInfo.setStatus(AuditDealStatus.FINISH);
						// 更新用户认证
						userIdentificationService.updateUserIdentification(userIdentificationInfo);
					}
					//实名认证更新后 删除redis数据
					redisTemplate.delete("biz_id".concat(user_id));
				}
				resp.SetResponseDetail(status);
			}
		} catch (Exception e) {
			log.error("人脸识别接口请求异常,id:"+user_id,e);
			resp.setStatus("9999");
		}

		return resp;
	}
	
}
