package com.gop.quantfund.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.QuantFundCodeConst;
import com.gop.domain.Finance;
import com.gop.domain.QuantFundReceiveIncomeDetail;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.domain.enums.QuantFundTransferType;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.quantfund.QuantFundConfigService;
import com.gop.quantfund.QuantFundReceiveIncomeDetailService;
import com.gop.quantfund.QuantFundService;
import com.gop.util.BigDecimalUtils;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QuantFundServiceImpl implements QuantFundService {
	
	@Autowired
	private FinanceService financeService;
	
	@Autowired
	private QuantFundConfigService quantFundConfigService;
	
	@Autowired
	private UserAccountFacade userAccountFacade;
	
	@Autowired
	private QuantFundReceiveIncomeDetailService quantFundReceiveIncomeDetailService;

	@Override
	@Transactional
	public void receiveQuantFundIncome(Integer uid, String fundAssetCode) {
		try {	
			Long endDate = Long.parseLong(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDENDDATE).getProfileValue());
			if (new Date().getTime() < endDate) {
				throw new AppException(QuantFundCodeConst.QUNAT_FUND_NOT_END);
			}
			//查询用户基金资产余额
			Finance finance = financeService.queryAccount(uid, fundAssetCode);
			if(finance == null) {
				throw new AppException(CommonCodeConst.FIELD_ERROR);
			}
			if(BigDecimalUtils.isEqualZero(finance.getAmountAvailable())) {
				throw new AppException(QuantFundCodeConst.NO_QUANT_FUND_ASSET_CODE_AMOUNT);
			}
			//收益比例
			BigDecimal fundIncomeRate = new BigDecimal(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDINCOMERATE).getProfileValue());
			//基金账户
			Integer fundUid = Integer.valueOf(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDUID).getProfileValue());
			//收益币种类型
			String incomeAssetCode = quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDINCOMEASSETCODE).getProfileValue();
			//用户基金余额
			BigDecimal fundAmount = finance.getAmountAvailable();
			//用户基金收益金额
			BigDecimal incomeAmount = fundAmount.multiply(fundIncomeRate);
			//为用户增加收益资产
			String userIncomePlusRequestNo = SequenceUtil.getNextId();
			AssetOperationDto userIncomePlusDto = new AssetOperationDto();
			userIncomePlusDto.setUid(uid);
			userIncomePlusDto.setRequestNo(userIncomePlusRequestNo);
			userIncomePlusDto.setBusinessSubject(BusinessSubject.QUANT_FUND_INCOME_TRANSFER);
			userIncomePlusDto.setAssetCode(incomeAssetCode);
			userIncomePlusDto.setAmount(incomeAmount);
			userIncomePlusDto.setLockAmount(BigDecimal.ZERO);
			userIncomePlusDto.setLoanAmount(BigDecimal.ZERO);
			userIncomePlusDto.setAccountClass(AccountClass.ASSET);
			userIncomePlusDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			userIncomePlusDto.setIndex(0);
			
			QuantFundReceiveIncomeDetail userIncomePlusDetail = new QuantFundReceiveIncomeDetail();
			userIncomePlusDetail.setUid(uid); 
			userIncomePlusDetail.setAssetCode(incomeAssetCode);
			userIncomePlusDetail.setTransferType(QuantFundTransferType.IN);
			userIncomePlusDetail.setAmount(incomeAmount);
			userIncomePlusDetail.setRequestNo(userIncomePlusRequestNo);
			quantFundReceiveIncomeDetailService.addQuantFundReceiveIncomeDetail(userIncomePlusDetail);
			//为用户扣除基金资产
			String userFundLessRequestNo = SequenceUtil.getNextId();
			AssetOperationDto userFundLessDto = new AssetOperationDto();
			userFundLessDto.setUid(uid);
			userFundLessDto.setRequestNo(userFundLessRequestNo);
			userFundLessDto.setBusinessSubject(BusinessSubject.QUANT_FUND_INCOME_TRANSFER);
			userFundLessDto.setAssetCode(fundAssetCode);
			userFundLessDto.setAmount(fundAmount.negate());
			userFundLessDto.setLockAmount(BigDecimal.ZERO);
			userFundLessDto.setLoanAmount(BigDecimal.ZERO);
			userFundLessDto.setAccountClass(AccountClass.ASSET);
			userFundLessDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			userFundLessDto.setIndex(1);
			
			QuantFundReceiveIncomeDetail userFundLessDetail = new QuantFundReceiveIncomeDetail();
			userFundLessDetail.setUid(uid); 
			userFundLessDetail.setAssetCode(fundAssetCode);
			userFundLessDetail.setTransferType(QuantFundTransferType.OUT);
			userFundLessDetail.setAmount(fundAmount.negate());
			userFundLessDetail.setRequestNo(userFundLessRequestNo);
			quantFundReceiveIncomeDetailService.addQuantFundReceiveIncomeDetail(userFundLessDetail);
			//为基金账户增加基金资产
			String fundPlusRequestNo = SequenceUtil.getNextId();
			AssetOperationDto fundPlusDto = new AssetOperationDto();
			fundPlusDto.setUid(fundUid);
			fundPlusDto.setRequestNo(fundPlusRequestNo);
			fundPlusDto.setBusinessSubject(BusinessSubject.QUANT_FUND_INCOME_TRANSFER);
			fundPlusDto.setAssetCode(fundAssetCode);
			fundPlusDto.setAmount(fundAmount);
			fundPlusDto.setLockAmount(BigDecimal.ZERO);
			fundPlusDto.setLoanAmount(BigDecimal.ZERO);
			fundPlusDto.setAccountClass(AccountClass.ASSET);
			fundPlusDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			fundPlusDto.setIndex(2);
			
			QuantFundReceiveIncomeDetail fundPlusDetail = new QuantFundReceiveIncomeDetail();
			fundPlusDetail.setUid(fundUid); 
			fundPlusDetail.setAssetCode(fundAssetCode);
			fundPlusDetail.setTransferType(QuantFundTransferType.IN);
			fundPlusDetail.setAmount(fundAmount);
			fundPlusDetail.setRequestNo(fundPlusRequestNo);
			quantFundReceiveIncomeDetailService.addQuantFundReceiveIncomeDetail(fundPlusDetail);
			//为基金账户扣除收益资产
			String fundIncomeLessRequestNo = SequenceUtil.getNextId();
			AssetOperationDto fundIncomeLessDto = new AssetOperationDto();
			fundIncomeLessDto.setUid(fundUid);
			fundIncomeLessDto.setRequestNo(fundIncomeLessRequestNo);
			fundIncomeLessDto.setBusinessSubject(BusinessSubject.QUANT_FUND_INCOME_TRANSFER);
			fundIncomeLessDto.setAssetCode(incomeAssetCode);
			fundIncomeLessDto.setAmount(incomeAmount.negate());
			fundIncomeLessDto.setLockAmount(BigDecimal.ZERO);
			fundIncomeLessDto.setLoanAmount(BigDecimal.ZERO);
			fundIncomeLessDto.setAccountClass(AccountClass.ASSET);
			fundIncomeLessDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			fundIncomeLessDto.setIndex(3);
			
			QuantFundReceiveIncomeDetail fundIncomeLessDetail = new QuantFundReceiveIncomeDetail();
			fundIncomeLessDetail.setUid(fundUid); 
			fundIncomeLessDetail.setAssetCode(incomeAssetCode);
			fundIncomeLessDetail.setTransferType(QuantFundTransferType.OUT);
			fundIncomeLessDetail.setAmount(incomeAmount.negate());
			fundIncomeLessDetail.setRequestNo(fundIncomeLessRequestNo);
			quantFundReceiveIncomeDetailService.addQuantFundReceiveIncomeDetail(fundIncomeLessDetail);

			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList();
			assetOperationDtos.add(userIncomePlusDto);
			assetOperationDtos.add(userFundLessDto);
			assetOperationDtos.add(fundPlusDto);
			assetOperationDtos.add(fundIncomeLessDto);
			userAccountFacade.assetOperation(assetOperationDtos);
		}catch (Exception e) {
			log.error("用户兑换量化基金收益异常userId={},eMessage=" + e.getMessage(),uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}
}
