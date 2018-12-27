package com.gop.user.service.impl;

import java.math.BigDecimal;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.UserCodeConst;

import com.gop.domain.JubiUserInformation;
import com.gop.domain.JubiUserInformationDetail;
import com.gop.domain.enums.JubiRegisterFlag;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.JubiUserInformationMapper;
import com.gop.user.service.JubiUserInfoDetailService;
import com.gop.user.service.JubiUserInfoService;
import com.gop.util.SequenceUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JubiUserInfoServiceImpl implements JubiUserInfoService {
	
	@Autowired
	private JubiUserInformationMapper jubiUserInformationMapper;
		
	@Autowired
	private UserAccountFacade UserAccountFacade;
	
	@Autowired
	private JubiUserInfoDetailService jubiUserInfoDetailService;
	
	
	
	@Override
	public List<JubiUserInformation> getJubiUserInfosByPhone(String phone) {
		List<JubiUserInformation> jubiInfos = jubiUserInformationMapper.selectByPhone(phone);
		if(null == jubiInfos || jubiInfos.isEmpty()) {
			throw new AppException(UserCodeConst.JUBI_USER_NOT_EXIST);
		}
		return jubiInfos;
	}

	@Override
	public List<JubiUserInformation> getJubiUserInfosByEmail(String email) {
		List<JubiUserInformation> jubiInfos = jubiUserInformationMapper.selectByEmail(email);
		if(null == jubiInfos || jubiInfos.isEmpty()) {
			throw new AppException(UserCodeConst.JUBI_USER_NOT_EXIST);
		}
		return jubiInfos;
	}

	@Override
	@Transactional
	public void assetOperationJubiUser(Integer uid,BigDecimal amount,Integer jubiId,String assetCode) {
		try {
			boolean flag = updateJubiUserFlagRegister(jubiId,JubiRegisterFlag.REGISTER);
			if (!flag) {
				throw new AppException(CommonCodeConst.SERVICE_ERROR);
			}
			String requestNo = SequenceUtil.getNextId();
			//聚币用户流水表新增一条记录
			JubiUserInformationDetail jubiUserInformationDetail = new JubiUserInformationDetail();
			jubiUserInformationDetail.setUid(uid);
			jubiUserInformationDetail.setJubiId(jubiId);
			jubiUserInformationDetail.setAssetCode(assetCode);
			jubiUserInformationDetail.setAmount(amount);
			jubiUserInformationDetail.setRequestNo(requestNo);
			jubiUserInfoDetailService.addJubiUserInformationDetail(jubiUserInformationDetail);
			
			AssetOperationDto assetOperationDto = new AssetOperationDto();
			assetOperationDto.setUid(uid);
			assetOperationDto.setRequestNo(requestNo);
			assetOperationDto.setBusinessSubject(BusinessSubject.JUBI_TRANSFER_IN);
			assetOperationDto.setAssetCode(assetCode);
			assetOperationDto.setAmount(amount);
			assetOperationDto.setLockAmount(BigDecimal.ZERO);
			assetOperationDto.setLoanAmount(BigDecimal.ZERO);
			assetOperationDto.setAccountClass(AccountClass.ASSET);
			assetOperationDto.setAccountSubject(AccountSubject.DEPOSIT_COIN);
			assetOperationDto.setIndex(0);
			List<AssetOperationDto> assetOperationDtos = Lists.newArrayList(assetOperationDto);
			//给聚币用户加数字资产
			UserAccountFacade.assetOperation(assetOperationDtos);
			
		} catch (Exception e) {
			log.error("为聚币用户账户添加ACT资产异常userId={},eMessage=" + e.getMessage(),uid, e);
			Throwables.propagateIfInstanceOf(e, AppException.class);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		return;	
	}

	@Override
	public boolean updateJubiUserFlagRegister(Integer jubiId,JubiRegisterFlag flag) {
		if(jubiUserInformationMapper.updateJubiUserFlagRegister(jubiId,flag) > 0) {
			return true;
		}
		return false;
	}
	
}
