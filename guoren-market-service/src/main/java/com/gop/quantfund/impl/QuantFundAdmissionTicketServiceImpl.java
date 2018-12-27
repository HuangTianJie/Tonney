package com.gop.quantfund.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import com.gop.domain.QuantFundAdmissionTicket;
import com.gop.domain.QuantFundAdmissionTicketBuyDetail;
import com.gop.domain.QuantFundAdmissionTicketReturnDetail;
import com.gop.domain.QuantFundTicketPreReturnTemp;
import com.gop.domain.enums.QuantFundAdmissionTicketStatus;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.domain.enums.QuantFundTransferType;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.QuantFundAdmissionTicketMapper;
import com.gop.quantfund.QuantFundAdmissionTicketBuyDetailService;
import com.gop.quantfund.QuantFundAdmissionTicketReturnDetailService;
import com.gop.quantfund.QuantFundAdmissionTicketService;
import com.gop.quantfund.QuantFundConfigService;
import com.gop.quantfund.QuantFundTicketPreReturnTempService;
import com.gop.util.BigDecimalUtils;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QuantFundAdmissionTicketServiceImpl implements QuantFundAdmissionTicketService {
	
	@Autowired
	private QuantFundAdmissionTicketMapper quantFundAdmissionTicketMapper;
	
	@Autowired
	private UserAccountFacade userAccountFacade;
	
	@Autowired
	private QuantFundAdmissionTicketBuyDetailService quantFundAdmissionTicketBuyDetailService;
	
	@Autowired
	private QuantFundAdmissionTicketReturnDetailService quantFundAdmissionTicketReturnDetailService;
	
	@Autowired
	private QuantFundConfigService quantFundConfigService;
	
	@Autowired
	private FinanceService financeService;
	
	@Autowired
	private QuantFundTicketPreReturnTempService quantFundTicketPreReturnTempService;
	
	@Override
	public QuantFundAdmissionTicket getQuantFundAdmissionTicketByUidAndFundAssetCode(Integer uid,
			String fundAssetCode) {
		return quantFundAdmissionTicketMapper.getQuantFundAdmissionTicketByUidAndFundAssetCode(uid, fundAssetCode);
	}

	@Override
	@Transactional
	public void buyQuantFundAdmissionTicket(Integer uid, String fundAssetCode) {
		if (!"on".equalsIgnoreCase(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETSTATUS).getProfileValue())) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_NOT_ON);
		}
		//校验
		if (null != getQuantFundAdmissionTicketByUidAndFundAssetCode(uid, fundAssetCode)) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_PURCHASED);
		}
		//查询config表
		String lockAssetCode = quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETASSETCODE).getProfileValue();
		BigDecimal lockAmount = new BigDecimal(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETASSETAMOUNT).getProfileValue());
		Integer ticketUid = Integer.valueOf(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETUID).getProfileValue());
		Finance finance = financeService.queryAccount(uid, lockAssetCode);
		if (null == finance) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_BUY_TICKET_ASSET_LESS);
		}
		if (BigDecimalUtils.isBigger(lockAmount, finance.getAmountAvailable())){
			throw new AppException(QuantFundCodeConst.QUANT_FUND_BUY_TICKET_ASSET_LESS);
		}
		try {
			//入场券表新增一条记录
			QuantFundAdmissionTicket quantFundAdmissionTicket = new QuantFundAdmissionTicket();
			quantFundAdmissionTicket.setUid(uid);
			quantFundAdmissionTicket.setFundAssetCode(fundAssetCode);
			quantFundAdmissionTicket.setLockAssetCode(lockAssetCode);
			quantFundAdmissionTicket.setLockAmount(lockAmount);
			quantFundAdmissionTicket.setStatus(QuantFundAdmissionTicketStatus.PURCHASED);
			addQuantFundAdmissionTicket(quantFundAdmissionTicket);
			//为购买入场券的用户扣除资产
			String outRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketBuyDetail quantFundAdmissionTicketBuyDetail = new QuantFundAdmissionTicketBuyDetail();
			quantFundAdmissionTicketBuyDetail.setTicketId(quantFundAdmissionTicket.getId());
			quantFundAdmissionTicketBuyDetail.setUid(uid);
			quantFundAdmissionTicketBuyDetail.setFundAssetCode(fundAssetCode);
			quantFundAdmissionTicketBuyDetail.setLockAssetCode(lockAssetCode);
			quantFundAdmissionTicketBuyDetail.setTransferType(QuantFundTransferType.OUT);
			quantFundAdmissionTicketBuyDetail.setAmount(lockAmount.negate());
			quantFundAdmissionTicketBuyDetail.setRequestNo(outRequestNo);
			quantFundAdmissionTicketBuyDetailService.addQuantFundAdmissionTicketBuyDetail(quantFundAdmissionTicketBuyDetail);
			
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(outRequestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_BUY);
			assetOperationDto.setAssetCode(lockAssetCode);
			assetOperationDto.setAmount(lockAmount.negate());
			assetOperationDto.setLockAmount(BigDecimal.ZERO);
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			assetOperationDto.setIndex(0);
			
			//为交易所内部账户增加资产
			String inRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketBuyDetail newquantFundAdmissionTicketBuyDetail = new QuantFundAdmissionTicketBuyDetail();
			newquantFundAdmissionTicketBuyDetail.setTicketId(quantFundAdmissionTicket.getId());
			newquantFundAdmissionTicketBuyDetail.setUid(ticketUid);
			newquantFundAdmissionTicketBuyDetail.setFundAssetCode(fundAssetCode);
			newquantFundAdmissionTicketBuyDetail.setLockAssetCode(lockAssetCode);
			newquantFundAdmissionTicketBuyDetail.setTransferType(QuantFundTransferType.IN);
			newquantFundAdmissionTicketBuyDetail.setAmount(lockAmount);
			newquantFundAdmissionTicketBuyDetail.setRequestNo(inRequestNo);
			quantFundAdmissionTicketBuyDetailService.addQuantFundAdmissionTicketBuyDetail(newquantFundAdmissionTicketBuyDetail);
			
			AssetOperationDto newassetOperationDto = new AssetOperationDto();
			newassetOperationDto.setUid(ticketUid);
			newassetOperationDto.setRequestNo(inRequestNo);
			newassetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_BUY);
			newassetOperationDto.setAssetCode(lockAssetCode);
			newassetOperationDto.setAmount(lockAmount);
			newassetOperationDto.setLockAmount(BigDecimal.ZERO);
			newassetOperationDto.setLoanAmount(BigDecimal.ZERO);
			newassetOperationDto.setAccountClass(AccountClass.ASSET);
			newassetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			newassetOperationDto.setIndex(1);
			
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList();
			assetOperationDtos.add(assetOperationDto);
			assetOperationDtos.add(newassetOperationDto);
			userAccountFacade.assetOperation(assetOperationDtos);
		} 
		catch (DuplicateKeyException e) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_PURCHASED);
		}
		catch (Exception e) {
			log.error("用户购买量化基金入场券异常userId={},eMessage=" + e.getMessage(),uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public void addQuantFundAdmissionTicket(QuantFundAdmissionTicket quantFundAdmissionTicket) {
		quantFundAdmissionTicketMapper.addQuantFundAdmissionTicket(quantFundAdmissionTicket);
	}

	@Override
	@Transactional
	public void returnQuantFundAdmissionTicket(Integer uid, String fundAssetCode) {
		//校验基金是否到期
		Long endDate = Long.parseLong(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.FUNDENDDATE).getProfileValue());
		if (new Date().getTime() < endDate) {
			throw new AppException(QuantFundCodeConst.QUNAT_FUND_NOT_END);
		}
		//查询用户入场券
		QuantFundAdmissionTicket quantFundAdmissionTicket = getQuantFundAdmissionTicketByUidAndFundAssetCode(uid,fundAssetCode);
		if (null ==quantFundAdmissionTicket) {
			throw new AppException(QuantFundCodeConst.NO_QUANT_FUND_TIKET);
		}
		if (QuantFundAdmissionTicketStatus.RETURNED.equals(quantFundAdmissionTicket.getStatus())) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_RETURNED);
		}
		//查询config表
		Integer ticketUid = Integer.valueOf(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETUID).getProfileValue());
		try {
			//更新入场券状态为已退还
			if(updateQuantFundAdmissionTicketStatusById(quantFundAdmissionTicket.getId(),QuantFundAdmissionTicketStatus.PURCHASED,QuantFundAdmissionTicketStatus.RETURNED) !=1 ) {
				throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_RETURNED);
			}
			//退还入场券押金流水表新增记录
			String inRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketReturnDetail quantFundAdmissionTicketReturnDetail = new QuantFundAdmissionTicketReturnDetail();
			quantFundAdmissionTicketReturnDetail.setTicketId(quantFundAdmissionTicket.getId());
			quantFundAdmissionTicketReturnDetail.setUid(uid);
			quantFundAdmissionTicketReturnDetail.setFundAssetCode(quantFundAdmissionTicket.getFundAssetCode());
			quantFundAdmissionTicketReturnDetail.setUnlockAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			quantFundAdmissionTicketReturnDetail.setTransferType(QuantFundTransferType.IN);
			quantFundAdmissionTicketReturnDetail.setAmount(quantFundAdmissionTicket.getLockAmount());
			quantFundAdmissionTicketReturnDetail.setRequestNo(inRequestNo);
			quantFundAdmissionTicketReturnDetailService.addQuantFundAdmissionTicketReturnDetail(quantFundAdmissionTicketReturnDetail);
			//为用户增加入场券押金资产
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(inRequestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_RETURN);
			assetOperationDto.setAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			assetOperationDto.setAmount(quantFundAdmissionTicket.getLockAmount());
			assetOperationDto.setLockAmount(BigDecimal.ZERO);
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			assetOperationDto.setIndex(0);
			
			//为交易所内部账户扣除押金资产
			String outRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketReturnDetail newquantFundAdmissionTicketReturnDetail = new QuantFundAdmissionTicketReturnDetail();
			newquantFundAdmissionTicketReturnDetail.setTicketId(quantFundAdmissionTicket.getId());
			newquantFundAdmissionTicketReturnDetail.setUid(ticketUid);
			newquantFundAdmissionTicketReturnDetail.setFundAssetCode(quantFundAdmissionTicket.getFundAssetCode());
			newquantFundAdmissionTicketReturnDetail.setUnlockAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			newquantFundAdmissionTicketReturnDetail.setTransferType(QuantFundTransferType.OUT);
			newquantFundAdmissionTicketReturnDetail.setAmount(quantFundAdmissionTicket.getLockAmount().negate());
			newquantFundAdmissionTicketReturnDetail.setRequestNo(outRequestNo);
			quantFundAdmissionTicketReturnDetailService.addQuantFundAdmissionTicketReturnDetail(newquantFundAdmissionTicketReturnDetail);
			
			AssetOperationDto newassetOperationDto = new AssetOperationDto();
			newassetOperationDto.setUid(ticketUid);
			newassetOperationDto.setRequestNo(outRequestNo);
			newassetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_RETURN);
			newassetOperationDto.setAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			newassetOperationDto.setAmount(quantFundAdmissionTicket.getLockAmount().negate());
			newassetOperationDto.setLockAmount(BigDecimal.ZERO);
			newassetOperationDto.setLoanAmount(BigDecimal.ZERO);
			newassetOperationDto.setAccountClass(AccountClass.ASSET);
			newassetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			newassetOperationDto.setIndex(1);
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList();
			assetOperationDtos.add(assetOperationDto);
			assetOperationDtos.add(newassetOperationDto);
			userAccountFacade.assetOperation(assetOperationDtos);
		} 
		catch (Exception e) {
			log.error("用户退还量化基金入场券押金异常userId={},eMessage=" + e.getMessage(),uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}
	@Override
	public int updateQuantFundAdmissionTicketStatusById(Integer id, QuantFundAdmissionTicketStatus beginStatus,
			QuantFundAdmissionTicketStatus endStatus) {
		return quantFundAdmissionTicketMapper.updateQuantFundAdmissionTicketStatusById(id, beginStatus, endStatus);
	}

	@Override
	@Transactional
	public void preReturnQuantFundAdmissionTicket(Integer uid, String fundAssetCode) {
		QuantFundTicketPreReturnTemp quantFundTicketPreReturnTemp  = quantFundTicketPreReturnTempService.getQuantFundTicketPreReturnTemp(uid, fundAssetCode);
		if (null ==quantFundTicketPreReturnTemp) {
			throw new AppException(QuantFundCodeConst.CAN_NOT_PRE_RETURN_TICKET);
		}
		//查询用户入场券
		QuantFundAdmissionTicket quantFundAdmissionTicket = getQuantFundAdmissionTicketByUidAndFundAssetCode(uid,fundAssetCode);
		if (null ==quantFundAdmissionTicket) {
			throw new AppException(QuantFundCodeConst.NO_QUANT_FUND_TIKET);
		}
		if (QuantFundAdmissionTicketStatus.RETURNED.equals(quantFundAdmissionTicket.getStatus())) {
			throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_RETURNED);
		}
		//查询config表
		Integer ticketUid = Integer.valueOf(quantFundConfigService.getConfigValueByCodeAndKey(fundAssetCode, QuantFundConfigType.TICKETUID).getProfileValue());
		try {
			//更新入场券状态为已退还
			if(updateQuantFundAdmissionTicketStatusById(quantFundAdmissionTicket.getId(),QuantFundAdmissionTicketStatus.PURCHASED,QuantFundAdmissionTicketStatus.RETURNED) !=1 ) {
				throw new AppException(QuantFundCodeConst.QUANT_FUND_TICKET_HAS_RETURNED);
			}
			//退还入场券押金流水表新增记录
			String inRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketReturnDetail quantFundAdmissionTicketReturnDetail = new QuantFundAdmissionTicketReturnDetail();
			quantFundAdmissionTicketReturnDetail.setTicketId(quantFundAdmissionTicket.getId());
			quantFundAdmissionTicketReturnDetail.setUid(uid);
			quantFundAdmissionTicketReturnDetail.setFundAssetCode(quantFundAdmissionTicket.getFundAssetCode());
			quantFundAdmissionTicketReturnDetail.setUnlockAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			quantFundAdmissionTicketReturnDetail.setTransferType(QuantFundTransferType.IN);
			quantFundAdmissionTicketReturnDetail.setAmount(quantFundAdmissionTicket.getLockAmount());
			quantFundAdmissionTicketReturnDetail.setRequestNo(inRequestNo);
			quantFundAdmissionTicketReturnDetailService.addQuantFundAdmissionTicketReturnDetail(quantFundAdmissionTicketReturnDetail);
			//为用户增加入场券押金资产
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(inRequestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_RETURN);
			assetOperationDto.setAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			assetOperationDto.setAmount(quantFundAdmissionTicket.getLockAmount());
			assetOperationDto.setLockAmount(BigDecimal.ZERO);
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_PLUS);
			assetOperationDto.setIndex(0);
			
			//为交易所内部账户扣除押金资产
			String outRequestNo = SequenceUtil.getNextId();
			QuantFundAdmissionTicketReturnDetail newquantFundAdmissionTicketReturnDetail = new QuantFundAdmissionTicketReturnDetail();
			newquantFundAdmissionTicketReturnDetail.setTicketId(quantFundAdmissionTicket.getId());
			newquantFundAdmissionTicketReturnDetail.setUid(ticketUid);
			newquantFundAdmissionTicketReturnDetail.setFundAssetCode(quantFundAdmissionTicket.getFundAssetCode());
			newquantFundAdmissionTicketReturnDetail.setUnlockAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			newquantFundAdmissionTicketReturnDetail.setTransferType(QuantFundTransferType.OUT);
			newquantFundAdmissionTicketReturnDetail.setAmount(quantFundAdmissionTicket.getLockAmount().negate());
			newquantFundAdmissionTicketReturnDetail.setRequestNo(outRequestNo);
			quantFundAdmissionTicketReturnDetailService.addQuantFundAdmissionTicketReturnDetail(newquantFundAdmissionTicketReturnDetail);
			
			AssetOperationDto newassetOperationDto = new AssetOperationDto();
			newassetOperationDto.setUid(ticketUid);
			newassetOperationDto.setRequestNo(outRequestNo);
			newassetOperationDto.setBusinessSubject(BusinessSubject.QUANT_FUND_TICKET_RETURN);
			newassetOperationDto.setAssetCode(quantFundAdmissionTicket.getLockAssetCode());
			newassetOperationDto.setAmount(quantFundAdmissionTicket.getLockAmount().negate());
			newassetOperationDto.setLockAmount(BigDecimal.ZERO);
			newassetOperationDto.setLoanAmount(BigDecimal.ZERO);
			newassetOperationDto.setAccountClass(AccountClass.ASSET);
			newassetOperationDto.setAccountSubject(AccountSubject.SYSTEM_TRANSFER_ASSET_LESS);
			newassetOperationDto.setIndex(1);
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList();
			assetOperationDtos.add(assetOperationDto);
			assetOperationDtos.add(newassetOperationDto);
			userAccountFacade.assetOperation(assetOperationDtos);
		} 
		catch (Exception e) {
			log.error("用户退还量化基金入场券押金异常userId={},eMessage=" + e.getMessage(),uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}
}
