package com.gop.asset.facade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.gop.domain.StatisticeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.FinanceDetailDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.domain.Finance;
import com.gop.domain.FinanceDetail;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAccountFacadeImpl implements UserAccountFacade {

	@Autowired
	private FinanceService financeService;

	@Autowired
	private ConfigAssetService configAssetService;

	@Override
	public Boolean createAccount(Integer userId, Integer brokerId, String assetCode, String accountKind) {

		return financeService.createFinanceAccount(userId, brokerId, assetCode, accountKind);
	}

	@Override
	public UserAccountDto queryAccount(Integer userId, String assetCode) {
		Finance finance = financeService.queryAccount(userId, assetCode);

		if (null == finance) {
			log.info("查询用户资产账号异常,uid:{},assetCode:{}", userId, assetCode);
			throw new AppException(UserAssetCodeConst.USER_ACCOUNT_NOT_EXIST);
		}

		UserAccountDto dto = new UserAccountDto();
		dto.setAccountKind(finance.getAccountKind());
		dto.setAccountId(finance.getId());
		dto.setAccountNo(finance.getAccountNo());
		dto.setAmountAvailable(finance.getAmountAvailable());
		dto.setAmountLoan(finance.getAmountLoan());
		dto.setAmountLock(finance.getAmountLock());
		dto.setAssetCode(finance.getAssetCode());
		dto.setUpdateDate(finance.getUpdateDate());
		dto.setUserId(userId);
		return dto;
	}

	@Override
	public List<FinanceDetailDto> queryFinanceDetailDto(Integer userId, String assetCode, Integer pageSize,
			Integer pageNo) {
		PageInfo<FinanceDetail> details = financeService.queryFinanceDetail(userId, assetCode, pageSize, pageNo);

		return details.getList().stream().map(detail -> new FinanceDetailDto(detail)).collect(Collectors.toList());
	}

	@Override
	public void assetOperation(List<AssetOperationDto> ops) {
		
		
		
		financeService.updateUserFinanceList(ops);

	}

	@Override
	public List<ConfigAsset> getAvailableAssetCode() {
		return configAssetService.getAvailableAssetCode();
	}

	public static void main(String[] args) {

		List<AssetOperationDto> ops = new ArrayList<>();
		AssetOperationDto assetOperationDto = new AssetOperationDto();
		assetOperationDto.setUid(4);
		ops.add(assetOperationDto);
		AssetOperationDto assetOperationDto2 = new AssetOperationDto();
		assetOperationDto2.setUid(4);
		ops.add(assetOperationDto2);
		Map<Integer, List<AssetOperationDto>> map = ops.stream()
				.collect(Collectors.groupingBy(AssetOperationDto::getUid));
		map.keySet().stream().sorted().forEach(new Consumer<Integer>() {

			@Override
			public void accept(Integer t) {
				System.out.println(t);
			}
		});

	}

	@Override
	public BigDecimal queryTotalCountByAssetCode(String assetCode) {
		List<StatisticeResult> statisticeResults = financeService.queryTotalCountByAssetCode(assetCode);
		return statisticeResults.get(0).getTradeNumber();
	}

}
