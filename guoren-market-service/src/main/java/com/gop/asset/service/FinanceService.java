package com.gop.asset.service;

import com.github.pagehelper.PageInfo;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.domain.*;
import com.gop.domain.enums.CurrencyType;
import com.gop.domain.enums.OptType;
import com.gop.mapper.dto.FinanceAmountDto;

import java.util.Date;
import java.util.List;

/**
 * 调用FinanceCheck的清算接口业务层，为检查用户的账是否平衡使用
 * 
 * @author liuze
 *
 */
public interface FinanceService {

	public Boolean createFinanceAccount(Integer userId, Integer brokerId, String assetCode, String accountKind);

	public Finance queryAccount(Integer userId, String assetCode);

	public List<Finance> queryAccounts(Integer userId);

	public Finance getAccountAndLock(Integer id);

	public Finance getAccountAndLock(Integer userId, String assetCode);

	PageInfo<FinanceDetail> queryFinanceDetail(Integer userId, String assetCode, Integer pageSize, Integer pageNo);
	
	PageInfo<FinanceDetail> queryFinanceDetailByType(Integer userId, OptType type, Integer pageSize, Integer pageNo);
	
	PageInfo<Finance> queryFinanceByBroker(Integer brokerId, Integer uid, Integer pageSize, Integer pageNo);

	public boolean saveFinance(Finance finance);

	public boolean updateFinanceByVersion(Finance finance);

	public boolean createFinanceDetail(FinanceDetail detail);

	public List<Long> updateAndProduceLogs(List<AssetOperationDto> ops);

	public void commitAssetOperation(List<Long> ids);
	
	public void clearFinance(List<AssetOperationDto> dtos);

	/**
	 * 核算资产
	 * @param ops
	 */
	public void updateUserFinanceList(List<AssetOperationDto> ops);
	
	List<StatisticeResult> queryTotalCountByAssetCode(String assetCode);

	List<WalletBalance> queryWalletBalance(String type, String assetCode, Date date, String account,
																						 Integer pageNo, Integer pageSize);

	List<WalletBalanceTotal> queryWalletTotal(String assetCode,Date date, Integer pageNo, Integer pageSize);

	List<WalletInOut> queryWalletInOut(Date beginDate,Date endDate, String opt, String assetCode, String account, Integer pageNo,
																				 Integer pageSize);

	PageInfo<FinanceAmountDto> getTotalAccountByAssetCode(String assetCode, Integer pageNo, Integer pageSize);

	List<FinanceAmountDto> getTotalAccountNoPage(String assetCode);

	List<WalletInOutTotal> queryWalletInOutTotal(String assetCode,Date beginDate, Date endDate, Integer pageNo, Integer pageSize);

	PageInfo<Finance> selectUserAccountList(Integer uid,String assetCode,Integer pageNo,Integer pageSize);

	List<Finance> getUserAccountList(Integer uid, String assetCode);

	public List<Finance> selectUserAccountListByCurrencyType(Integer uid, CurrencyType currencyType);

	void sendEmailReport(String assetCode,Date beginDate, Date endDate);
}
