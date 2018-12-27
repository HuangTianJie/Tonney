package com.gop.currency.transfer.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.currency.transfer.service.UnCerfDepositCurrencyOrderService;
import com.gop.domain.DepositCurrencyOrderUser;
import com.gop.domain.enums.DepositCurrencyOrderStatus;
import com.gop.domain.enums.DepositCurrencyPayMode;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.DepositCurrencyOrderUserMapper;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.util.OrderUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnCerfDepositCurrencyOrderServiceImpl implements UnCerfDepositCurrencyOrderService {

	@Autowired
	private UserAccountFacade userAccountFacade;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	private DepositCurrencyOrderUserMapper depositCurrencyOrderUserMapper;

	@Override
	public void currencyDepositOrderUnCerf(int uid, String outOrder, String assetCode, BigDecimal amount) {
	
		Integer accountId = userAccountFacade.queryAccount(uid, assetCode).getAccountId();

		UserSimpleInfoDto user = userFacade.getUserInfoByUid(uid);

		String txid = OrderUtil.generateCode(OrderUtil.PAY_SERVICE, OrderUtil.TRANSFER_IN_CURRENCY);

		DepositCurrencyOrderUser order = new DepositCurrencyOrderUser();
		order.setAccountId(accountId);
		order.setBrokerId(user.getBrokerId());
		order.setAccount(user.getUserAccount());
		order.setAcnumber("");
		order.setAssetCode(assetCode);
		order.setBank("");
		order.setStatus(DepositCurrencyOrderStatus.SUCCESS);
		order.setFee(BigDecimal.ZERO);
		order.setMoney(amount);
		order.setPay(amount);
		order.setMsg("券商充值");
		order.setName("");
		order.setPayMode(DepositCurrencyPayMode.OFFLINE);
		order.setUid(uid);
		order.setTxid(txid);

		order.setCreateDate(new Date());
		order.setUpdateDate(new Date());
		try {
			depositCurrencyOrderUserMapper.insert(order);
		} catch (RuntimeException e) {
			log.info("添加银行卡充值订单失败,用户订单信息为:{}", order);
		}
		
		List<AssetOperationDto> ops = new ArrayList<>();

		AssetOperationDto depositDto = new AssetOperationDto();
		depositDto.setAccountClass(AccountClass.LIABILITY);
		depositDto.setAccountSubject(AccountSubject.DEPOSIT_COMMON);
		depositDto.setAssetCode(order.getAssetCode());
		depositDto.setBusinessSubject(BusinessSubject.DEPOSIT);
		depositDto.setAmount(amount);
		depositDto.setLoanAmount(BigDecimal.ZERO);
		depositDto.setLockAmount(BigDecimal.ZERO);
		depositDto.setMemo(order.getAssetCode()+"充值");
		depositDto.setRequestNo(txid);
		depositDto.setUid(uid);
		ops.add(depositDto);

		try {
			userAccountFacade.assetOperation(ops);
		} catch (Exception e) {
			log.info("给用户加钱失败:{}", e.getMessage());
			throw e;
		}

	}

}
