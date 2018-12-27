package com.gop.asset.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;
import com.gop.asset.Mode.vo.RealTimeClearInfo;
import com.gop.asset.service.FinanceCheckClearing;
import com.gop.asset.service.FinanceCheckClearingNet;
import com.gop.code.consts.CommonCodeConst;
import com.gop.exception.AppException;
import com.gop.mode.vo.RealtimeClearResult;
import com.gop.util.BigDecimalUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FinanceCheckClearingImpl implements FinanceCheckClearing {
	@Autowired
	private FinanceCheckClearingNet financeCheckClearingNet;

	@Value("${finance_check_clearing_need_broker}")
	private String financeCheckClearingNeedBroker;

	@Override
	public Boolean financeCheckClearing(Integer version, Integer brokerId, BigDecimal exceptedNum, Integer uid, String accountNo,
			String assetCode) {
		Boolean flag = false;

		// 清算白名单，查询不需要清算的白名单
		if (financeCheckClearingNeedBroker == null) {
			log.info("financeCheckClearingNeedBroker== null,没有需要清算的名单");
			return true;
		}
		
		String[] needBrokerIds = financeCheckClearingNeedBroker.split(",");
		for (String ss : needBrokerIds) {
			if (ss.equals(brokerId.toString())) {
				log.debug("brokerId={},在清算白名单当中，不必清算", brokerId);
				return true;
			}
		}

		// 1. CNY校验
		RealTimeClearInfo realTimeClearInfoCny = new RealTimeClearInfo();
		realTimeClearInfoCny.setUserId(uid);
		realTimeClearInfoCny.setAccountNo(accountNo);
		realTimeClearInfoCny.setBrokerId(brokerId);
		realTimeClearInfoCny.setAssetCode(assetCode);
		realTimeClearInfoCny.setBalanceVersion(version);

		// 调用清算接口service
		RealtimeClearResult financeCheckResultCny;
		try {
			log.info(realTimeClearInfoCny.toString());
			financeCheckResultCny = financeCheckClearingNet.financeCheckClearingNet(realTimeClearInfoCny);
			
		} catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, AppException.class);
			log.error("financeCheckClearing网络层异常", e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "服务异常");
		}

		BigDecimal endBalanceCny = BigDecimal.ZERO;
		if (financeCheckResultCny == null) {
			log.info("FinanceCheck状态正常但没数据,uid=" + uid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "服务异常,uid=" + uid);
		}
		log.info("清算服务返回数据" + financeCheckResultCny.toString());
		endBalanceCny = financeCheckResultCny.getEndBalance();

		// 进行比对 cnySum和拿到金额进行比对
		if (!(exceptedNum.compareTo(endBalanceCny) == 0) || !BigDecimalUtils.isEqualZero(financeCheckResultCny.getDiffAmount())) {
			log.error(
					"用户CNY账不平,uid={},本地finance cny={},financeCheck接口取得 endcny=,{},difAmount={}",uid,exceptedNum,endBalanceCny,financeCheckResultCny.getDiffAmount());
			return false;
		}

		flag = true;
		return flag;

	}

}
