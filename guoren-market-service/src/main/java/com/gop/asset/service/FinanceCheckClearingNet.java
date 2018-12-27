package com.gop.asset.service;

import com.gop.asset.Mode.vo.RealTimeClearInfo;
import com.gop.mode.vo.RealtimeClearResult;

/**
 * 调用FinanceCheck的清算接口网络层，为检查用户的账是否平衡使用
 * 
 * @author liuze
 *
 */
public interface FinanceCheckClearingNet {
	RealtimeClearResult financeCheckClearingNet(RealTimeClearInfo runtimeClearInfo);
}
