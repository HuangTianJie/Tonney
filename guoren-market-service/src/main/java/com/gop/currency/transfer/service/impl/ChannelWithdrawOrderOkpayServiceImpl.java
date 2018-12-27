package com.gop.currency.transfer.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.WithdrawalsCodeConst;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.config.OkpayConstants;
import com.gop.currency.transfer.service.ChannelCurrencyWithdrawOrderService;
import com.gop.domain.ChannelOkpayUserAccount;
import com.gop.domain.WithdrawCurrencyOrderUser;
import com.gop.domain.enums.WithdrawCurrencyOrderStatus;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.ChannelOkpayUserAccountMapper;
import com.gop.mapper.WithdrawCurrencyOrderUserMapper;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.util.BigDecimalUtils;
import com.gop.util.OrderUtil;

import lombok.extern.slf4j.Slf4j;

@Service("withdrawOrderOkpayServiceImpl")
@Slf4j
public class ChannelWithdrawOrderOkpayServiceImpl implements ChannelCurrencyWithdrawOrderService {@Override
	public void withdrawOrder(Integer channelWithdrawId, String externalOrderNo, String assetCode, Integer uid,
			BigDecimal money, String memo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void withdrawOrder(int uid, String assetCode, String externalOrderNo, String accountName, BigDecimal money,
			BigDecimal fee, String bankName, String bankNo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BigDecimal getWithdrawFee(String assetCode, BigDecimal money) {
		// TODO Auto-generated method stub
		return null;
	}

}
