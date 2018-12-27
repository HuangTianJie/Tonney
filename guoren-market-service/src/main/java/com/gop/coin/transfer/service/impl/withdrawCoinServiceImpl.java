package com.gop.coin.transfer.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MessageConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.service.ConfigAssetProfileService;
import com.gop.coin.transfer.service.DepositCoinAddressService;
import com.gop.coin.transfer.service.DepositCoinOrderService;
import com.gop.coin.transfer.service.WithdrawCoinService;
import com.gop.coin.transfer.service.CheckWithdrawCoinService;
import com.gop.common.Environment;
import com.gop.common.SendMessageService;
import com.gop.common.SmsMessageService;
import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.ChannelCoinAddressWithdraw;
import com.gop.domain.ConfigAsset;
import com.gop.domain.WithdrawCoinOrderUser;
import com.gop.domain.dto.TransferCoinMessage;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gop.domain.enums.DestAddressType;
import com.gop.domain.enums.InnerAddressFlag;
import com.gop.domain.enums.WithdrawCoinOrderStatus;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mapper.ChannelCoinAddressWithdrawMapper;
import com.gop.mapper.ConfigAssetProfileMapper;
import com.gop.mapper.WithdrawCoinOrderUserMapper;
import com.gop.mode.vo.ProduceLogVo;
import com.gop.user.dto.UserSimpleInfoDto;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserMessageService;
import com.gop.util.BigDecimalUtils;
import com.gop.util.DateUtils;
import com.gop.util.OrderUtil;

import lombok.extern.slf4j.Slf4j;

@Service("withdrawSelfService")
@Slf4j
public class withdrawCoinServiceImpl implements WithdrawCoinService {

	@Autowired
	Environment environmentContxt;

	@Autowired
	private UserFacade userFacade;
	@Autowired
	private ConfigAssetService configAssetService;
	@Autowired
	private DepositCoinOrderService depositCoinOrderService;
	@Autowired
	private UserMessageService userMessageService;

	@Autowired
	private SmsMessageService smsMessageService;

	@Autowired
	private UserAccountFacade userAccountFacade;

	@Autowired
	private WithdrawCoinOrderUserMapper withdrawCoinOrderUserMapper;

	@Autowired
	private SendMessageService sendMessageService;

	@Value("${exchange}")
	private String exchange;

	@Autowired
	private UserAccountFacade accountFacade;

	@Autowired
	private ConfigAssetProfileService configAssetProfileService;

	@Autowired
	private DepositCoinAddressService depositCoinAddressService;

	@Autowired
	private ChannelCoinAddressWithdrawMapper channelCoinAddressWithdrawMapper;

	@Autowired
	private WithdrawCoinService withdrawCoinService;

	@Autowired
	private CheckWithdrawCoinService checkWithdrawCoinService;

	@Override
	@Transactional
	public void withdrawCoinOrder(int uid, String outOrder, String assetCode, BigDecimal amount, BigDecimal fee,
			String address, String message) {

		UserSimpleInfoDto user = userFacade.getUserInfoByUid(uid);

		ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
		if (null == configAsset) {
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE, "无效的用户资产");
		}

		UserAccountDto account = accountFacade.queryAccount(uid, assetCode);
		if (null == account) {
			log.info("获取用户账号异常,assetCode:{},uid:{}", assetCode, uid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "账号状态异常");
		}
		// 1.5.1版本修改
		// 使用helper处理
		checkWithdrawCoinService.checkWithdrawCoinOrder(uid, user.getAuthLevel(), assetCode, amount);

		fee = checkWithdrawCoinService.getWithdrawAmount(assetCode, ConfigAssetType.WITHDRAWMINFEE);

		ChannelCoinAddressWithdraw userWithdrawAddress = channelCoinAddressWithdrawMapper
				.selectByAddressAndAssetCodeNotDel(address, assetCode, uid);

		InnerAddressFlag flag = depositCoinAddressService.checkIsInnerAddress(address, assetCode);

		DestAddressType addressType = null;
		if (flag.equals(InnerAddressFlag.YES)) {
			addressType = DestAddressType.INNER_ADDRESS;
			fee = new BigDecimal(0);
		} else {
			addressType = DestAddressType.OUTER_ADDRESS;
		}

		BigDecimal realNumber = amount.subtract(fee);
		String txId = OrderUtil.generateCode(OrderUtil.TRANSFER_SERVICE, OrderUtil.TRANSFER_OUT_COIN);

		WithdrawCoinOrderUser order = new WithdrawCoinOrderUser();
		order.setAccountId(account.getAccountId());
		order.setBrokerId(user.getBrokerId());
		order.setAccount(user.getUserAccount());
		order.setAssetCode(assetCode);
		if (null == userWithdrawAddress) {
			order.setChannelWithdrawId(0);

		} else {
			order.setChannelWithdrawId(userWithdrawAddress.getId());

		}

		order.setCoinAddress(address);
		order.setCreateDate(new Date());
		order.setDestAddressType(addressType);
		order.setInnerOrderNo(txId);
		order.setOuterOrderNo(outOrder);
		order.setMsg(message);
		order.setTxFee(fee);
		order.setNumber(amount);
		order.setRealNumber(realNumber);
		order.setStatus(WithdrawCoinOrderStatus.WAIT);
		order.setUid(uid);
		order.setUpdateDate(new Date());

		// 生成提现订单
		if (!addressType.equals(DestAddressType.INNER_ADDRESS)) {
			order.setStatus(WithdrawCoinOrderStatus.WAIT);

			if (withdrawCoinOrderUserMapper.insertSelective(order) < 1) {
				log.error("用户转出订单添加失败,uid:{},assetCode{},amount{}", uid, assetCode, amount);
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单添加失败");
			}

			// 用户扣款
			deductMoney(order);
		} else {

			log.info("内部用户互转");
			// 不是内部地址需要审核
			order.setStatus(WithdrawCoinOrderStatus.WAIT);
			withdrawCoinOrderUserMapper.insertSelective(order);
			//            if (withdrawCoinOrderUserMapper.insertSelective(order) < 1) {
			//                log.error("用户转出订单添加失败,uid:{},assetCode{},amount{}", uid, assetCode, amount);
			//                throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单添加失败");
			//            }
			// 转出用户扣款
			List<AssetOperationDto> ops = new ArrayList<>();
			AssetOperationDto withdrawDto = new AssetOperationDto();
			withdrawDto.setAccountClass(AccountClass.LIABILITY);
			withdrawDto.setAccountSubject(AccountSubject.WITHDRAW_COIN);
			withdrawDto.setAmount(BigDecimal.ZERO.subtract(order.getRealNumber()));
			withdrawDto.setAssetCode(order.getAssetCode());
			withdrawDto.setBusinessSubject(BusinessSubject.WITHDRAW);
			withdrawDto.setLoanAmount(BigDecimal.ZERO);
			withdrawDto.setLockAmount(order.getRealNumber());
			withdrawDto.setMemo(assetCode + "提现");
			withdrawDto.setRequestNo(order.getInnerOrderNo());
			withdrawDto.setUid(order.getUid());
			withdrawDto.setIndex(0);
			//            AssetOperationDto feeDto = new AssetOperationDto();
			//            feeDto.setAccountClass(AccountClass.LIABILITY);
			//            feeDto.setAccountSubject(AccountSubject.FEE_WITHDRAW_SPEND);
			//            feeDto.setAmount(BigDecimal.ZERO.subtract(order.getTxFee()));
			//            feeDto.setAssetCode(order.getAssetCode());
			//            feeDto.setBusinessSubject(BusinessSubject.FEE);
			//            feeDto.setLoanAmount(BigDecimal.ZERO);
			//            feeDto.setLockAmount(BigDecimal.ZERO);
			//            feeDto.setMemo(assetCode + "提现手续费");
			//            feeDto.setRequestNo(order.getInnerOrderNo());
			//            feeDto.setUid(order.getUid());
			//            feeDto.setIndex(1);
			ops.add(withdrawDto);
			//            ops.add(feeDto);

			// 转入用户收款
			ChannelCoinAddressDeposit depositAddress = depositCoinAddressService.getCoinDepositAddress(address,
					assetCode);

			// 为内部用户生成充值订单

			depositCoinOrderService.depositConfirm(assetCode, depositAddress.getCoinAddress(), realNumber,
					order.getInnerOrderNo(), order.getMsg());

			// 发送转出消息
			sendMessage(order);

			try {
				userAccountFacade.assetOperation(ops);
			} catch (Exception e) {
				log.info("修改用户资产失败:{}", e.getMessage());
				throw e;
			}
		}

	}

	@Override
	@Transactional
	public void withdraw(int id, int adminId) {

		WithdrawCoinOrderUser order = withdrawCoinOrderUserMapper.selectForUpdate(id);

		if (null == order) {
			log.info("无效转出订单，id:{}", id);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		if("OUTER_ADDRESS".equals(order.getDestAddressType().toString())) {
			String key = String.format("gdae2.transferOut.%s.key", order.getAssetCode().toLowerCase());

			TransferCoinMessage transferCoinMessage = new TransferCoinMessage();
			transferCoinMessage.setTxid(order.getInnerOrderNo());
			transferCoinMessage.setAddress(order.getCoinAddress());
			transferCoinMessage.setAmount(order.getRealNumber().toString());
			transferCoinMessage.setTxfee(order.getTxFee().toString());
			transferCoinMessage.setMessage(order.getMsg());

			ProduceLogVo produceLogVo = new ProduceLogVo();
			produceLogVo.setExchangeName(exchange);
			produceLogVo.setKey(key);
			produceLogVo.setMessage(transferCoinMessage);
			Long messageId = sendMessageService.tryMessage(produceLogVo);

			WithdrawCoinOrderStatus status = order.getStatus();
			if (!status.equals(WithdrawCoinOrderStatus.WAIT) && !status.equals(WithdrawCoinOrderStatus.UNKNOWN)) {
				log.info("订单状态异常，id:{}", id);
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单状态异常");
			}
			order.setStatus(WithdrawCoinOrderStatus.PROCESSING);
			order.setUpdateDate(new Date());

			try {
				withdrawCoinOrderUserMapper.updateByPrimaryKeySelective(order);
			} catch (Exception e) {
				sendMessageService.rollBackMessage(messageId);
			}

			try {
				sendMessageService.commitMessage(messageId);
			} catch (Exception e1) {
				log.error("order error" + e1);
				Throwables.propagate(new AppException(CommonCodeConst.SERVICE_ERROR, "发送消息到mq失败"));
			}

			log.info("普通用户果仁转出: 发送消息到路由MQ,transferCoinMessage={}", transferCoinMessage);
		}else {
			//解冻
			List<AssetOperationDto> ops = new ArrayList<>();
			AssetOperationDto withdrawDto = new AssetOperationDto();
			withdrawDto.setAccountClass(AccountClass.LIABILITY);
			withdrawDto.setAccountSubject(AccountSubject.WITHDRAW_COIN);
			withdrawDto.setAmount(BigDecimal.ZERO);
			withdrawDto.setAssetCode(order.getAssetCode());
			withdrawDto.setBusinessSubject(BusinessSubject.WITHDRAW);
			withdrawDto.setLoanAmount(BigDecimal.ZERO);
			withdrawDto.setLockAmount(BigDecimal.ZERO.subtract(order.getRealNumber()));
			withdrawDto.setMemo(order.getAssetCode() + "内转通过提现解冻");
			withdrawDto.setRequestNo(order.getInnerOrderNo());
			withdrawDto.setUid(order.getUid());
			withdrawDto.setIndex(0);
			ops.add(withdrawDto);
			userAccountFacade.assetOperation(ops);
			// 为内部用户生成充值订单
			depositCoinOrderService.depositConfirm(order.getAssetCode(), order.getCoinAddress(), order.getRealNumber(),
					order.getInnerOrderNo(), order.getMsg());
			sendMessage(order);
		}

	}

	@Override
	@Transactional
	public void withdrawConfirm(String txid,BigDecimal chainFee) {
		WithdrawCoinOrderUser order = withdrawCoinOrderUserMapper.selectByInnerOrder(txid);
		if (order == null) {
			log.info("订单{}不存在", txid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		WithdrawCoinOrderStatus status = order.getStatus();

		if (!status.equals(WithdrawCoinOrderStatus.PROCESSING)) {
			if (status.equals(WithdrawCoinOrderStatus.SUCCESS)) {
				log.info("订单已经被处理，消息重复.txid:{}", txid);
				return;
			} else {
				log.info("订单状态异常，txid:{}", txid);
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单状态异常");
			}
		}

		order = withdrawCoinOrderUserMapper.selectForUpdate(order.getId());
		if (order.getStatus() != WithdrawCoinOrderStatus.PROCESSING) {
			log.info("订单{}状态异常", txid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		order.setStatus(WithdrawCoinOrderStatus.SUCCESS);
		order.setUpdateDate(new Date());
		order.setChainFee(chainFee);
		if (withdrawCoinOrderUserMapper.updateByPrimaryKeySelective(order) < 1) {
			log.info("更新订单{}失败", txid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		sendMessage(order);// 发送通知短信或邮件
		// 回调

	}

	@Override
	@Transactional
	public void withdrawRefund(int id, int adminId, String refuseMs) {
		Date currentDate = new Date();

		int result = 0;

		WithdrawCoinOrderUser order = withdrawCoinOrderUserMapper.selectForUpdate(id);
		if("OUTER_ADDRESS".equals(order.getDestAddressType().toString())) {

			if (!order.getStatus().equals(WithdrawCoinOrderStatus.WAIT)
					&& !order.getStatus().equals(WithdrawCoinOrderStatus.UNKNOWN)) {
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单状态异常");
			}
			order.setStatus(WithdrawCoinOrderStatus.REFUSE);
			order.setMsg(refuseMs);
			order.setUpdateDate(currentDate);
			result = withdrawCoinOrderUserMapper.updateByPrimaryKeySelective(order);
			if (result <= 0) {
				log.info("果仁转出-审核不通过:更新转帐记录时失败,转帐id:{}", order.getId());
				throw new AppException("更新转帐记录时失败");
			}

			returnMoney(order);
		}else {
			//解冻
			List<AssetOperationDto> ops = new ArrayList<>();
			AssetOperationDto withdrawDto = new AssetOperationDto();
			withdrawDto.setAccountClass(AccountClass.LIABILITY);
			withdrawDto.setAccountSubject(AccountSubject.WITHDRAW_COIN);
			withdrawDto.setAmount(order.getRealNumber());
			withdrawDto.setAssetCode(order.getAssetCode());
			withdrawDto.setBusinessSubject(BusinessSubject.WITHDRAW);
			withdrawDto.setLoanAmount(BigDecimal.ZERO);
			withdrawDto.setLockAmount(BigDecimal.ZERO.subtract(order.getRealNumber()));
			withdrawDto.setMemo(order.getAssetCode() + "内转不通过提现解冻");
			withdrawDto.setRequestNo(order.getInnerOrderNo());
			withdrawDto.setUid(order.getUid());
			withdrawDto.setIndex(0);
			ops.add(withdrawDto);
			userAccountFacade.assetOperation(ops);
		}

		sendMessage(order);// 发送通知短信或邮件
	}

	@Override
	@Transactional
	public void withdrawFail(String txid, String msg) {
		WithdrawCoinOrderUser order = withdrawCoinOrderUserMapper.selectByInnerOrder(txid);
		if (order == null) {
			log.info("订单{}不存在", txid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		order = withdrawCoinOrderUserMapper.selectForUpdate(order.getId());
		if (order.getStatus() != WithdrawCoinOrderStatus.PROCESSING) {
			log.error("订单{}状态异常", txid);
			return;
		}
		order.setStatus(WithdrawCoinOrderStatus.UNKNOWN);
		order.setUpdateDate(new Date());
		order.setMsg(msg);
		if (withdrawCoinOrderUserMapper.updateByPrimaryKeySelective(order) < 1) {
			log.info("更新订单{}失败", txid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

	}

	public DestAddressType getAddressType(String assetCode, InnerAddressFlag addressFlag) {
		if (addressFlag.equals(InnerAddressFlag.YES)) {
			return DestAddressType.INNER_ADDRESS;
		}
		return DestAddressType.OUTER_ADDRESS;
	}

	public void sendMessage(WithdrawCoinOrderUser transferOut) {
		String transferMessage = null;

		try {
			transferMessage = generateTransferMessage(transferOut);
			userMessageService.insertMessage(transferOut.getUid(), transferMessage);

			UserSimpleInfoDto user = userFacade.getUserInfoByUid(transferOut.getUid());
			log.info("给用户{}发送邮件，信息:{}", user.getUserAccount(), transferMessage);
			smsMessageService.sendEmailMessage(user.getUserAccount(), transferMessage);

		} catch (Exception e) {
			log.error("果仁转入正常，短信及站内信异常, 用户id={}, 短信内容={}", transferOut.getUid(), transferMessage, e);
		}

	}

	public String generateTransferMessage(WithdrawCoinOrderUser order) {

		String message = null;
		int newScale = configAssetService.getAssetConfig(order.getAssetCode()).getMinPrecision();
		switch (order.getStatus()) {
		case SUCCESS:
			message = environmentContxt.getMsg(MessageConst.COIN_WITHDRAW_SUCCESS_MESSAGE,
					order.getNumber().setScale(newScale, RoundingMode.FLOOR).toPlainString(), order.getAssetCode(),
					DateUtils.formatDate(order.getCreateDate()));
			break;
		case FAILURE:
			message = environmentContxt.getMsg(MessageConst.COIN_WITHDRAW_FAIL_MESSAGE,
					order.getNumber().setScale(newScale, RoundingMode.FLOOR).toPlainString(), order.getAssetCode(),
					DateUtils.formatDate(order.getCreateDate()));
			break;
		case REFUSE:
			message = environmentContxt.getMsg(MessageConst.COIN_WITHDRAW_REFUSEM_MESSAGE,
					order.getNumber().setScale(newScale, RoundingMode.FLOOR).toPlainString(), order.getAssetCode(),
					DateUtils.formatDate(order.getCreateDate()));
			break;
		default:
			break;
		}

		return message;
	}

	private void deductMoney(WithdrawCoinOrderUser order) {
		List<AssetOperationDto> ops = new ArrayList<>();
		AssetOperationDto withdrawDto = new AssetOperationDto();
		withdrawDto.setAccountClass(AccountClass.LIABILITY);
		withdrawDto.setAccountSubject(AccountSubject.WITHDRAW_COMMON);
		withdrawDto.setAmount(BigDecimal.ZERO.subtract(order.getRealNumber()));
		withdrawDto.setAssetCode(order.getAssetCode());
		withdrawDto.setBusinessSubject(BusinessSubject.WITHDRAW);
		withdrawDto.setLoanAmount(BigDecimal.ZERO);
		withdrawDto.setLockAmount(BigDecimal.ZERO);
		withdrawDto.setMemo(order.getAssetCode() + "提现");
		withdrawDto.setRequestNo(order.getInnerOrderNo());
		withdrawDto.setUid(order.getUid());

		AssetOperationDto feeDto = new AssetOperationDto();
		feeDto.setAccountClass(AccountClass.LIABILITY);
		feeDto.setAccountSubject(AccountSubject.FEE_WITHDRAW_SPEND);
		feeDto.setAmount(BigDecimal.ZERO.subtract(order.getTxFee()));
		feeDto.setAssetCode(order.getAssetCode());
		feeDto.setBusinessSubject(BusinessSubject.FEE);
		feeDto.setLoanAmount(BigDecimal.ZERO);
		feeDto.setLockAmount(BigDecimal.ZERO);
		feeDto.setMemo(order.getAssetCode() + "提现手续费");
		feeDto.setRequestNo(order.getInnerOrderNo());
		feeDto.setUid(order.getUid());

		ops.add(withdrawDto);
		ops.add(feeDto);
		try {
			userAccountFacade.assetOperation(ops);
		} catch (Exception e) {
			log.info("修改用户资产失败:{}", e.getMessage());
			throw e;
		}
	}

	/**
	 * 给用户退款
	 *
	 * @param order
	 */
	private void returnMoney(WithdrawCoinOrderUser order) {

		List<AssetOperationDto> ops = new ArrayList<>();

		AssetOperationDto withdrawDto = new AssetOperationDto();
		withdrawDto.setAccountClass(AccountClass.LIABILITY);
		withdrawDto.setAccountSubject(AccountSubject.WITHDRAW_RETURN);
		withdrawDto.setAssetCode(order.getAssetCode());
		withdrawDto.setBusinessSubject(BusinessSubject.WITHDRAW_RETURN);
		withdrawDto.setAmount(order.getRealNumber());
		withdrawDto.setLoanAmount(BigDecimal.ZERO);
		withdrawDto.setLockAmount(BigDecimal.ZERO);
		withdrawDto.setMemo(order.getAssetCode() + "提现费用退回");
		withdrawDto.setRequestNo(order.getInnerOrderNo());
		withdrawDto.setUid(order.getUid());
		ops.add(withdrawDto);
		if (BigDecimalUtils.isBiggerZero(order.getTxFee())) {
			AssetOperationDto feeDto = new AssetOperationDto();
			feeDto.setAccountClass(AccountClass.LIABILITY);
			feeDto.setAccountSubject(AccountSubject.FEE_WITHDRAW_RETURN);
			feeDto.setAmount(order.getTxFee());
			feeDto.setAssetCode(order.getAssetCode());
			feeDto.setBusinessSubject(BusinessSubject.FEE_RETURN);
			feeDto.setLoanAmount(BigDecimal.ZERO);
			feeDto.setLockAmount(BigDecimal.ZERO);
			feeDto.setMemo(order.getAssetCode() + "提现手续费退回");
			feeDto.setRequestNo(order.getInnerOrderNo());
			feeDto.setUid(order.getUid());
			ops.add(feeDto);
		}
		try {
			userAccountFacade.assetOperation(ops);
		} catch (Exception e) {
			log.info("退款给用户失败:{}", e.getMessage());
			throw e;
		}
	}

	@Override
	public BigDecimal getWithdrawFee(BigDecimal amount) {
		throw new AppException(CommonCodeConst.SERVICE_ERROR, "btc的提现手续费由用户指定");
	}

	@Transactional
	public void withdrawCoinOrderUnCerf(int uid, String outOrder, String assetCode, BigDecimal amount) {
		BigDecimal gopWithdrawFeeAmount = configAssetProfileService.getBigDecimalValue(assetCode,
				ConfigAssetType.WITHDRAWMINFEE);

		UserAccountDto account = accountFacade.queryAccount(uid, assetCode);
		if (null == account) {
			log.info("获取用户账号异常,assetCode:{},uid:{}", assetCode, uid);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "账号状态异常");
		}

		BigDecimal realNumber = amount.subtract(gopWithdrawFeeAmount);
		String txId = OrderUtil.generateCode(OrderUtil.TRANSFER_SERVICE, OrderUtil.TRANSFER_OUT_COIN);

		UserSimpleInfoDto user = userFacade.getUserInfoByUid(uid);
		WithdrawCoinOrderUser order = new WithdrawCoinOrderUser();
		order.setAccountId(account.getAccountId());
		order.setBrokerId(user.getBrokerId());
		order.setAccount(user.getUserAccount());
		order.setAssetCode(assetCode);
		order.setChannelWithdrawId(0);
		order.setCoinAddress("");
		order.setCreateDate(new Date());
		order.setDestAddressType(DestAddressType.OUTER_ADDRESS);
		order.setInnerOrderNo(txId);
		order.setOuterOrderNo(outOrder);
		order.setMsg("券商用户" + uid + "提现");
		order.setNumber(amount);
		order.setTxFee(BigDecimal.ZERO);
		order.setRealNumber(realNumber);
		order.setUid(uid);
		order.setUpdateDate(new Date());
		// 生成提现订单
		order.setStatus(WithdrawCoinOrderStatus.SUCCESS);

		if (withdrawCoinOrderUserMapper.insertSelective(order) < 1) {
			log.info("用户转出订单添加失败,uid:{},assetCode{},amount{}", uid, assetCode, amount);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "订单添加失败");
		}

		// 用户扣款
		deductMoney(order);

	}

	@Override
	public BigDecimal getUserDailyWithdrawedCoinValue(Integer uid, String assetCode, Date beginDate, Date endDate) {
		return withdrawCoinOrderUserMapper.getUserDailyWithdrawedCoinValue(uid, assetCode, beginDate, endDate);
	}

	@Override
	public Integer getAmountOfAssetWithStatus(String assetCode, WithdrawCoinOrderStatus status) {
		try {
			return withdrawCoinOrderUserMapper.getAmountOfAssetWithStatus(assetCode, status);
		} catch (Exception e) {
			log.error("查询assetCode{},status{}状态数量失败", assetCode, status);
			throw new AppException(CommonCodeConst.SERVICE_ERROR, status + "状态订单数量查询失败");
		}
	}
}
