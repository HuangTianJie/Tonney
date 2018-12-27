package com.gop.asset.facade;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.FinanceDetailDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.domain.ConfigAsset;

import java.math.BigDecimal;
import java.util.List;

public interface UserAccountFacade {

	/**
	 * 创建账户
	 */
	public Boolean createAccount(Integer userId, Integer brokerId, String assetCode, String accountKind);

	/**
	 * 账户资产查询
 	 */
	public UserAccountDto queryAccount(Integer userId, String assetCode);

	/**
	 * 账户流水查询(参数为定义)
 	 */
	public List<FinanceDetailDto> queryFinanceDetailDto(Integer userId, String assetCode, Integer pageSize,
			Integer pageNo);

	/**
	 * 资产增减
	 * @param ops
	 */
	public void assetOperation(List<AssetOperationDto>  ops);
	
	public List<ConfigAsset> getAvailableAssetCode();

	/**
	 * 查询该资产在交易所的总额
	 * @param assetCode
	 * @return
	 */
	public BigDecimal queryTotalCountByAssetCode(String assetCode);
	
}
