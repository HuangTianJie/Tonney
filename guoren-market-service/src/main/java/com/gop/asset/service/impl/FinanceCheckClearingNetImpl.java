package com.gop.asset.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.gop.asset.Mode.vo.RealTimeClearInfo;
import com.gop.asset.Mode.vo.WebApiResponse;
import com.gop.asset.service.FinanceCheckClearingNet;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.ReconciliationCodeConst;
import com.gop.exception.AppException;
import com.gop.mode.vo.RealtimeClearResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 调用FinanceCheck的清算接口网络层实现，为检查用户的账是否平衡使用
 * 
 * @author liuze
 *
 */
@Service
@Slf4j
public class FinanceCheckClearingNetImpl implements FinanceCheckClearingNet {
	@Value("${financeCheck.url}")
	private String financeCheckUrl;

	@Override
	public RealtimeClearResult financeCheckClearingNet(RealTimeClearInfo realTimeClearInfo) {
		// 网络层
		WebApiResponse result = null;

		try {
			log.info("financeCheckUrl接口url：" + financeCheckUrl);
			RestTemplate restTemplate = new RestTemplate();
			result = restTemplate.postForEntity(financeCheckUrl, realTimeClearInfo, WebApiResponse.class).getBody();

		} catch (Exception e) {
			log.error("financeCheck clearing接口 异常", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, null);
		}

		if (result == null) {
			log.info("result为null，连状态码都没有" + result);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, null);
		}

		if (CommonCodeConst.SERIVCE_SUCCESS.equals(result.getCode())) {
			if (result.getData() == null) {
				log.info("result的Data为null,但状态成功");
				throw new AppException(CommonCodeConst.SERVICE_ERROR, result.getCode() + "_" + result.getMsg());
			}

			log.info("从financeCheck拿到的data数据：" + result.getData().toString());
			return JSONObject.parseObject(JSONObject.toJSONString(result.getData()), RealtimeClearResult.class);
		} else {
			log.error("financeCheck 返回的状态码不是200: " + result.getMsg());
			if (result.getCode().equals(ReconciliationCodeConst.ACCOUNT_INCONSISTENT)) {
				throw new AppException(ReconciliationCodeConst.ACCOUNT_INCONSISTENT, result.getMsg());
			}
			throw new AppException(CommonCodeConst.SERVICE_ERROR, result.getCode() + "_" + result.getMsg());

		}
	}


}
