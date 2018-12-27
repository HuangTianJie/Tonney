package com.gop.coin.transfer.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gop.domain.StatisticeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.AccountCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MessageConst;
import com.gop.code.consts.OrderCodeConst;
import com.gop.coin.transfer.dto.DepositCoinDto;
import com.gop.coin.transfer.service.DepositCoinAddressService;
import com.gop.coin.transfer.service.DepositCoinOrderService;
import com.gop.common.Environment;
import com.gop.common.SendMessageService;
import com.gop.common.SmsMessageService;
import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.DepositCoinOrderUser;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.domain.enums.DestAddressType;
import com.gop.domain.enums.InnerAddressFlag;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.DepositCoinOrderUserMapper;
import com.gop.mode.vo.ProduceLogVo;
import com.gop.notify.dto.NotifyDto;
import com.gop.notify.dto.enums.NotifyType;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserMessageService;
import com.gop.util.DateUtils;
import com.gop.util.OrderUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DepositCoinOrderServiceImpl implements DepositCoinOrderService {

	@Autowired
	private Environment environmentContxt;

	@Autowired
	private UserMessageService userMessageService;

	@Autowired
	private SmsMessageService smsMessageService;

	@Autowired
	private UserFacade userFace;

	@Autowired
	private DepositCoinOrderUserMapper depositCoinOrderUserMapper;

	@Autowired
	private DepositCoinAddressService depositCoinAddressService;

	@Autowired
	private UserFacade userFacade;

	@Autowired
	private UserAccountFacade userAccountFacade;

	@Value("${exchange}")
	private String exchange;

	@Override
	@Transactional
	public void coinDepositOrder(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage) {

		ChannelCoinAddressDeposit address = depositCoinAddressService.getCoinDepositAddress(toAddress, assetCode);

		if (address == null) {
			log.error("发现不存在的地址:{}", address);
			throw new AppException(AccountCodeConst.COIN_ADDRESS_INVALID);
		}

		Integer uid = address.getUid();

		UserSimpleInfoDto user = userFacade.getUserInfoByUid(uid);
		UserAccountDto useAccount = userAccountFacade.queryAccount(uid, assetCode);

		int brokerId = user.getBrokerId();
		int accountId = useAccount.getAccountId();
		String account = user.getUserAccount();
		Integer channelDepositId = address.getId();
		InnerAddressFlag innerAddressFlag = depositCoinAddressService.checkIsInnerAddress(toAddress, assetCode);

		DestAddressType destAddressType;
		if (innerAddressFlag.equals(InnerAddressFlag.YES)) {
			destAddressType = DestAddressType.INNER_ADDRESS;
		} else if (innerAddressFlag.equals(InnerAddressFlag.NO)) {
			destAddressType = DestAddressType.OUTER_ADDRESS;
		} else {
			log.info("无效地址类型");
			throw new AppException(AccountCodeConst.COIN_ADDRESS_INVALID, "无效地址类型");
		}

		String txId = OrderUtil.generateCode(OrderUtil.ORDER_SERVICE, OrderUtil.TRANSFER_IN_COIN);

		DepositCoinOrderUser order = new DepositCoinOrderUser();

		order.setUid(uid);
		order.setBrokerId(brokerId);
		order.setAccountId(accountId);
		order.setAccount(account);
		order.setAssetCode(assetCode);
		order.setChannelDepositId(channelDepositId);
		order.setCoinAddress(toAddress);
		order.setOuterOrderNo(outerOrder);
		order.setInnerOrderNo(txId);
		order.setNumber(amount);
		order.setRealNumber(amount);
		order.setFee(BigDecimal.ZERO);

		order.setDestAddressType(destAddressType);
		order.setMsg(sendMessage);
		order.setCreateDate(new Date());
		order.setUpdateDate(new Date());
		order.setAssetStatus(DepositCoinAssetStatus.CONFIRM);
		try {
			if (depositCoinOrderUserMapper.insertSelective(order) < 1) {
				log.info("数据库插入资产{}失败,外部单号:{}", assetCode, outerOrder);
				throw new AppException(CommonCodeConst.SERVICE_ERROR);
			}
		} catch (DuplicateKeyException e) {
			log.info("重复插入资产{},外部单号:{}", assetCode, outerOrder);
		}

	}

	@Override
	@Transactional
	public void depositConfirm(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage) {
		DepositCoinOrderUser order = depositCoinOrderUserMapper.selectByOutOrder(assetCode, outerOrder);

		if (order == null) {
			log.info("{}充值订单{}不存在", assetCode, outerOrder);
			// 订单不存在
			coinDepositOrder(assetCode, toAddress, amount, outerOrder, sendMessage);
		}
		order = depositCoinOrderUserMapper.selectByOutOrder(assetCode, outerOrder);

		order = depositCoinOrderUserMapper.selectForUpdate(order.getId());

		if (order.getAssetStatus() == DepositCoinAssetStatus.SUCCESS) {
			log.info("{}充值订单{}已被确认", assetCode, outerOrder);
			return;
		}

		if (order.getAssetStatus() != DepositCoinAssetStatus.CONFIRM) {
			log.info("{}充值订单{}状态异常，无法入账", assetCode, outerOrder);
			throw new AppException(OrderCodeConst.ORDER_STATUS_ERROR, "", order.getAssetStatus());
		}

		order.setAssetStatus(DepositCoinAssetStatus.SUCCESS);

		order.setUpdateDate(new Date());

		if (depositCoinOrderUserMapper.updateByPrimaryKeySelective(order) < 1) {
			log.info("更新数据库资产{}失败,外部单号:{}", assetCode, outerOrder);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		// 通过订单给用户入账
		transferInAccount(order);
		try {
			sendMessage(order);
		} catch (Exception e) {
			log.error("入账消息发送失败:", e);
		}

	}

	@Override
	@Transactional
	public void depositCancel(String assetCode, String toAddress, BigDecimal amount, String outerOrder,
			String sendMessage) {
		DepositCoinOrderUser order = depositCoinOrderUserMapper.selectByOutOrder(assetCode, outerOrder);
		if (order == null) {
			log.info("{}充值订单{}不存在", assetCode, outerOrder);

			coinDepositOrder(assetCode, toAddress, amount, outerOrder, sendMessage);
		}

		order = depositCoinOrderUserMapper.selectByOutOrder(assetCode, outerOrder);

		order = depositCoinOrderUserMapper.selectForUpdate(order.getId());
		if (order.getAssetStatus() == DepositCoinAssetStatus.FAILURE) {
			log.info("{}充值订单{}已被取消", assetCode, outerOrder);
			return;
		}

		if (order.getAssetStatus() != DepositCoinAssetStatus.CONFIRM) {
			log.info("{}充值订单{}状态异常，无法入账", assetCode, outerOrder);
			throw new AppException(OrderCodeConst.ORDER_STATUS_ERROR, "", order.getAssetStatus());
		}

		order.setAssetStatus(DepositCoinAssetStatus.FAILURE);
		order.setUpdateDate(new Date());

		if (depositCoinOrderUserMapper.updateByPrimaryKeySelective(order) < 1) {
			log.info("更新数据库资产{}失败,外部单号:{}", assetCode, outerOrder);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

	}

	private void transferInAccount(DepositCoinOrderUser order) {
		List<AssetOperationDto> ops = new ArrayList<>();
		AssetOperationDto depositDto = new AssetOperationDto();
		depositDto.setAccountClass(AccountClass.LIABILITY);
		depositDto.setAccountSubject(AccountSubject.DEPOSIT_COIN);
		depositDto.setAmount(order.getRealNumber());
		depositDto.setAssetCode(order.getAssetCode());
		depositDto.setBusinessSubject(BusinessSubject.DEPOSIT);
		depositDto.setLoanAmount(BigDecimal.ZERO);
		depositDto.setLockAmount(BigDecimal.ZERO);
		depositDto.setMemo(order.getAssetCode() + "充值");
		depositDto.setRequestNo(order.getInnerOrderNo());
		depositDto.setUid(order.getUid());
		ops.add(depositDto);

		try {
			userAccountFacade.assetOperation(ops);
		} catch (Exception e) {
			log.info("修改用户资产失败:{}", e.getMessage());
			throw e;
		}
	}

	public String generateDepositSuccMessage(DepositCoinOrderUser order) {

		String message = null;

		int persion = getDepositMessagePersion(order.getAssetCode());

		switch (order.getAssetStatus()) {
		case SUCCESS:
			message = environmentContxt.getMsg(MessageConst.COIN_DEPOSIT_SUCCESS_MESSAGE,
					order.getRealNumber().setScale(persion, RoundingMode.FLOOR).toString(), order.getAssetCode(),
					DateUtils.formatDate(order.getCreateDate()));
			break;
		default:
			break;
		}
		return message;
	}

	private void sendMessage(DepositCoinOrderUser order) {
		try {
			String message = generateDepositSuccMessage(order);
			userMessageService.insertMessage(order.getUid(), message);

			UserSimpleInfoDto user = userFace.getUserInfoByUid(order.getUid());

			smsMessageService.sendEmailMessage(user.getUserAccount(), message);
		} catch (Exception e) {
			log.info("给用户发送消息失败");
		}
	}

	private int getDepositMessagePersion(String assetCode) {

		switch (assetCode) {
		case "GOP":
			return 8;
		case "BTC":
			return 8;
		default:
			return 8;
		}

	}

	@Override
	@Transactional
	public void coinDepositOrderUnCerf(int uid, String outOrder, String assetCode, BigDecimal amount) {
		UserSimpleInfoDto user = userFacade.getUserInfoByUid(uid);
		UserAccountDto useAccount = userAccountFacade.queryAccount(uid, assetCode);

		int brokerId = user.getBrokerId();
		int accountId = useAccount.getAccountId();
		String account = user.getUserAccount();

		String txId = OrderUtil.generateCode(OrderUtil.ORDER_SERVICE, OrderUtil.TRANSFER_IN_COIN);

		DepositCoinOrderUser order = new DepositCoinOrderUser();
		order.setUid(uid);
		order.setBrokerId(brokerId);
		order.setAccountId(accountId);
		order.setAccount(account);
		order.setAssetCode(assetCode);
		order.setChannelDepositId(0);
		order.setCoinAddress("");
		order.setOuterOrderNo(outOrder);
		order.setInnerOrderNo(txId);
		order.setNumber(amount);
		order.setRealNumber(amount);
		order.setFee(BigDecimal.ZERO);
		order.setDestAddressType(DestAddressType.OUTER_ADDRESS);
		order.setMsg("券商" + uid + "充值直接到账订单");
		order.setCreateDate(new Date());
		order.setUpdateDate(new Date());
		order.setAssetStatus(DepositCoinAssetStatus.SUCCESS);
		try {
			if (depositCoinOrderUserMapper.insertSelective(order) < 1) {
				log.info("数据库插入资产{}失败,外部单号:{}", assetCode, outOrder);
				throw new AppException(CommonCodeConst.SERVICE_ERROR);
			}
		} catch (DuplicateKeyException e) {
			log.info("重复插入资产{},外部单号:{}", assetCode, outOrder);
		}

		transferInAccount(order);

	}

}
