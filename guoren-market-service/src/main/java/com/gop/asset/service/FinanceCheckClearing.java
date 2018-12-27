package com.gop.asset.service;

import java.math.BigDecimal;

/**
 * 调用FinanceCheck的清算接口业务层，为检查用户的账是否平衡使用
 * 
 * @author liuze
 *
 */
public interface FinanceCheckClearing {
	public Boolean financeCheckClearing(Integer version,Integer brokerId, BigDecimal exceptedNum, Integer uid,  String accountNo, String assetCode);
}
