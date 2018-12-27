package com.gop.asset.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceCheckClearing;
import com.gop.asset.service.FinanceService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.ReconciliationCodeConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.coin.transfer.service.WithdrawCoinQueryService;
import com.gop.common.ExportExcelService;
import com.gop.domain.*;
import com.gop.domain.enums.*;
import com.gop.domain.request.WalletInOutQueryRequest;
import com.gop.domain.response.WalletResponseBase;
import com.gop.exception.AppException;
import com.gop.finance.service.BeginingBalanceService;
import com.gop.financecheck.domain.ItemFinanceHistory;
import com.gop.financecheck.enums.AccountChange;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.financecheck.helper.AccountServiceHelper;
import com.gop.mapper.FinanceDetailMapper;
import com.gop.mapper.FinanceMapper;
import com.gop.mapper.PlatAssetProessMapper;
import com.gop.mapper.dto.FinanceAmountDto;
import com.gop.match.service.TradeRecordService;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.EmailLogService;
import com.gop.sms.service.IEmailService;
import com.gop.user.facade.BrokerFacade;
import com.gop.util.BigDecimalUtils;
import com.gop.util.DateUtils;
import com.gop.util.SequenceUtil;
import com.gop.utils.HttpUtil;
import com.gop.utils.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//import com.gop.financecheck.enums.AssetType;

@Service
@Slf4j
public class FinanceServiceImpl implements FinanceService {
	private static final String TOTAL_FTL ="dailyTotal.ftl";
	private static final String TOTAL_NAME = "total.xls";
	@Value("${mail.to-user}")
	private String toUser;
	@Value("${mail.report-subject}")
	private String subject;
	@Value("${mail.report-text}")
	private String text;
	@Value("${mail.report-root}")
	private String rootPath;

	@Autowired
	private FinanceDetailMapper financeDetailMapper;

	@Autowired
	private FinanceMapper financeMapper;
	@Autowired
	private IEmailService iEmailService;
	@Autowired
	private EmailLogService	emailLogService;
	@Autowired
	private BrokerFacade brokerFacade;

	@Autowired
	private FinanceCheckClearing financeCheckClearing;

	@Autowired
	private ConfigAssetService configAssetService;

	@Autowired
	private DepositCoinQueryService depositCoinQueryService;
	@Autowired
	private WithdrawCoinQueryService withdrawCoinQueryService;
	@Autowired
	private TradeRecordService tradeRecordService;
	@Autowired
	private BeginingBalanceService beginingBalanceService;
	@Autowired
	private BrokerAssetOperDetailService brokerAssetOperDetailService;
	@Autowired
	private ExportExcelService exportExcelService;
	@Autowired
	private FinanceService financeService;
	@Autowired
	private PlatAssetProessMapper platAssetProessMapper;

	@Override
	public Boolean createFinanceAccount(Integer userId, Integer brokerId, String assetCode, String accountKind) {

		ConfigAsset config = configAssetService.getAssetConfig(assetCode);
		if (null == config) {
			log.error("创建资产失败,无效的资产代码:{}", assetCode);
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}

		if (!config.getStatus().equals(AssetStatus.LISTED)) {
			log.error("创建资产失败,资产未上市:{}", assetCode);
			throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
		}

		Finance finance = new Finance();

		finance.setAccountKind(accountKind);

		finance.setAssetCode(assetCode);

		// 用 分布式ID生成器生成唯一ID
		String accountNo = SequenceUtil.getNextId();
		finance.setAccountNo(accountNo);
		finance.setBrokerId(brokerId);
		finance.setAmountAvailable(BigDecimal.ZERO);
		finance.setAmountLoan(BigDecimal.ZERO);
		finance.setAmountLock(BigDecimal.ZERO);
		finance.setUid(userId);
		finance.setUpdateDate(new Date());
		finance.setVersion(0);
		boolean flag = true;
		try {
			flag = financeMapper.insert(finance) > 0;
		} catch (DuplicateKeyException e) {
			log.info("用户账户已存在");
		}
		return flag;
	}

	@Override
	public Finance queryAccount(Integer uid, String assetCode) {

		return financeMapper.selectByUidAndAssetCode(uid, assetCode);
	}

	@Override
	public PageInfo<FinanceDetail> queryFinanceDetail(Integer userId, String assetCode, Integer pageSize,
			Integer pageNo) {
		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy("id desc");
		return new PageInfo<>(financeDetailMapper.selectByUidAndAssetCode(userId, assetCode));

	}

	@Override
	public PageInfo<FinanceDetail> queryFinanceDetailByType(Integer userId, OptType type, Integer pageSize,
			Integer pageNo) {
		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy("id desc");

		int typeInt = 0;
		if (OptType.IN.equals(type)) {
			typeInt = 1;
		}
		if (OptType.OUT.equals(type)) {
			typeInt = -1;
		}

		return new PageInfo<FinanceDetail>(financeDetailMapper.selectByUidAndType(userId, typeInt));
	}

	@Override
	public Finance getAccountAndLock(Integer id) {
		return financeMapper.selectForUpdate(id);
	}

	@Override
	public Finance getAccountAndLock(Integer userId, String assetCode) {
		return financeMapper.selectByUidAndAssetCodeForUpdate(userId, assetCode);
	}

	@Override
	public boolean saveFinance(Finance finance) {
		return financeMapper.insert(finance) > 0 ? true : false;
	}

	@Override
	public boolean updateFinanceByVersion(Finance finance) {
		return financeMapper.updateFinanceWithVersion(finance) > 0 ? true : false;

	}

	@Override
	public boolean createFinanceDetail(FinanceDetail detail) {
		return financeDetailMapper.insertSelective(detail) > 0 ? true : false;
	}

	@Override
	public List<Finance> queryAccounts(Integer userId) {
		return financeMapper.selectByUid(userId);
	}

	@Override
	public void clearFinance(List<AssetOperationDto> dtos) {
		for (AssetOperationDto dto : dtos) {
			if (dto.getBusinessSubject().equals(BusinessSubject.WITHDRAW)) {
				clearFinance(dto.getUid());
			}
		}

	}

	public void clearFinance(Integer uid) {

		// 充值与提现才会启动清算
		List<ConfigAsset> confs = configAssetService.getAvailableAssetCode();

		for (ConfigAsset config : confs) {

			Finance finance = financeMapper.selectByUidAndAssetCode(uid, config.getAssetCode());
			if (finance == null) {
				continue;
			}
			int brokerId = brokerFacade.getBrokerIdByUid(uid);
			Boolean flag = true;
			BigDecimal exceptedNum;
			try {
				exceptedNum = finance.getAmountAvailable().add(finance.getAmountLock());
				flag = financeCheckClearing.financeCheckClearing(finance.getVersion(), brokerId, exceptedNum, uid,
						finance.getAccountNo(), finance.getAssetCode());
			} catch (Exception e) {
				log.error("清算服务异常", e);
				Throwables.propagateIfInstanceOf(e, AppException.class);
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "清算服务异常");
			}
			if (!flag) {
				log.error("用户账目异常,用户id{},{} 账面资产{},清算资产{}", finance.getUid(), finance.getAssetCode(), exceptedNum);
				throw new AppException(ReconciliationCodeConst.ACCOUNT_INCONSISTENT, "用户帐不平");
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public List<Long> updateAndProduceLogs(List<AssetOperationDto> ops) {
		// 更新资产并生成本地消息
		List<ItemFinanceHistory> items = updateBalance(ops);
		if (null == items) {
			return null;
		}
		// 提交消息到本地数据库
		return tryMessage(items);

	}

	@Deprecated
	public List<ItemFinanceHistory> updateBalance(List<AssetOperationDto> ops) {

		List<ItemFinanceHistory> items = new ArrayList<>();
		for (AssetOperationDto op : ops) {

			checkAssetOperationDto(op);

			boolean isProduceDetail = checkIsProduceDetail(op);

			Integer brokerId = brokerFacade.getBrokerIdByUid(op.getUid());
			AccountChange accountChange = getChangeByAmount(op.getAmount().add(op.getLockAmount()));

			ItemFinanceHistory itemHistory = updateBalance(op.getBusinessSubject(), op.getAssetCode(), brokerId,
					op.getAmount(), op.getLoanAmount(), op.getLockAmount(), op.getRequestNo(), op.getUid(),
					op.getAccountClass(), op.getAccountSubject(), accountChange, op.getMemo(), isProduceDetail);
			if (null != itemHistory) {
				items.add(itemHistory);
			} else {
				return null;
			}
		}
		return items;

	}

	@Deprecated
	private ItemFinanceHistory updateBalance(BusinessSubject businessSubject, String assetCode, Integer brokerId,
			BigDecimal amount, BigDecimal amountLoan, BigDecimal amountLock, String outTxNo, Integer uid,
			AccountClass accountClass, AccountSubject accountSubject, AccountChange accountChange, String memo,
			boolean isProduceDetail) {

		Date createDateNow = new Date();
		String accountNo = null;
		ItemFinanceHistory itemHistory = null;

		try {
			Finance finance = getAccountAndLock(uid, assetCode);

			if (finance == null) {
				log.error("用户{}资产{}账户不存在，或者锁定账户失败", uid, assetCode);

				throw new AppException(UserAssetCodeConst.USER_ACCOUNT_NOT_EXIST, "账户不存在，或者锁定账户失败");
			}

			accountNo = finance.getAccountNo();

			BigDecimal oldAmountAvailable = finance.getAmountAvailable();
			BigDecimal oldAmountLoan = finance.getAmountLoan();
			BigDecimal oldAmountLock = finance.getAmountLock();

			Finance newFinance = updateFinance(finance, amount, amountLock, amountLoan);

			// 更行资产
			if (!updateFinanceByVersion(newFinance)) {
				log.error("更新用户:{}资产表时发生错误, newItemFinance={}", uid, newFinance.toString());
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "更新用户" + assetCode + "资产错误");
			}

			BigDecimal balanceTotal = newFinance.getAmountAvailable().add(newFinance.getAmountLock());

			FinanceDetail detail = new FinanceDetail();
			detail.setRequestNo(outTxNo);
			detail.setUid(newFinance.getUid());
			detail.setAmountAvailable(amount);
			detail.setAmountLoan(amountLoan);
			detail.setAmountLock(amountLock);
			detail.setAssetCode(assetCode);
			detail.setBalanceNewAvailable(newFinance.getAmountAvailable());
			detail.setBalanceNewLoan(newFinance.getAmountLoan());
			detail.setBalanceNewLock(newFinance.getAmountLock());
			detail.setBalanceOldAvailable(oldAmountAvailable);
			detail.setBalanceOldLoan(oldAmountLoan);
			detail.setBalanceOldLock(oldAmountLock);
			detail.setBusinessSubject(businessSubject.toString());
			detail.setCreateDate(new Date());
			// 以后修改此处代码0:总资产未改变，1:总资产已改变
			if (businessSubject.equals(BusinessSubject.LOCK) || businessSubject.equals(BusinessSubject.UNLOCK)) {
				detail.setAssetChangeType(0);
			} else {
				detail.setAssetChangeType(1);
			}

			if (!createFinanceDetail(detail)) {
				log.error("资产{}流水生成失败,事务回滚", detail.toString());
				throw new AppException(CommonCodeConst.SERVICE_ERROR, "资产流水生成失败");
			}

			Integer createTime = (int) (createDateNow.getTime() / 1000);
			if (isProduceDetail) {
				// 构造交易流水
				// 现在取消商户与普通用户的区别，流水全部使用个人流水

				BigDecimal amount1 = amount.add(amountLock);
				itemHistory = AccountServiceHelper.buildFinanceHistoryItem(accountNo, accountClass, accountChange,
						brokerId, assetCode, uid, businessSubject, outTxNo, accountSubject, amount1, balanceTotal,
						newFinance.getVersion() + 1, createTime, memo);
			}
		} catch (DuplicateKeyException e) {
			log.error("订单{}已经被消费,回滚事务", outTxNo);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return null;
		}
		return itemHistory;
	}

	private List<Long> tryMessage(List<ItemFinanceHistory> items) {
		// return financeHistoryService.trySendQueueMessage(items);
		return null;
	}

	@Override
	public void commitAssetOperation(List<Long> ids) {
		//financeHistoryService.commitSendQueueMessage(ids);
	}

	@Deprecated
	public Finance updateFinance(Finance finance, BigDecimal aviAmount, BigDecimal lockAmount, BigDecimal loanAmount) {

		BigDecimal availableLive = finance.getAmountAvailable().add(aviAmount);
		BigDecimal lockLive = finance.getAmountLock().add(lockAmount);
		BigDecimal loanLive = finance.getAmountLoan().add(loanAmount);
		if (BigDecimalUtils.isLessZero(availableLive)) {
			log.error("用户={}, 资产＝{}，余额={}不足,修改后可用金额为{}", finance.getAccountNo(), finance.getAssetCode(),
					finance.getAmountAvailable(), availableLive);
			throw new AppException(UserAssetCodeConst.CURRENCY_ASSERT_LESS, "用户可用资产余额不足");

		}
		if (BigDecimalUtils.isLessZero(lockLive)) {
			log.error("用户={}, 资产＝{}，锁定余额={}不足,修改后余额为{}", finance.getAccountNo(), finance.getAssetCode(),
					finance.getAmountLock(), lockLive);
			throw new AppException(UserAssetCodeConst.CURRENCY_ASSERT_LESS, "用户锁定资产余额不足");
		}

		if (BigDecimalUtils.isLessZero(loanLive)) {
			log.error("用户={}, 资产＝{}，借贷余额={}不足,修改后借贷金额为{}", finance.getAccountNo(), finance.getAssetCode(),
					finance.getAmountLoan(), loanAmount);
			throw new AppException(UserAssetCodeConst.CURRENCY_ASSERT_LESS, "用户借贷资产余额不足");
		}

		finance.setAmountAvailable(availableLive);

		finance.setAmountLock(lockLive);
		finance.setAmountLoan(loanLive);
		finance.setUpdateDate(new Date());
		return finance;

	}

	@Deprecated
	private AccountChange getChangeByAmount(BigDecimal amount) {

		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			return AccountChange.PLUS;
		} else if (amount.compareTo(BigDecimal.ZERO) < 0) {
			return AccountChange.LESS;
		} else {
			return AccountChange.UNKNOWN;
		}

	}

	private void checkAssetOperationDto(AssetOperationDto dto) {
		if (null == dto.getLoanAmount() || null == dto.getLockAmount() || null == dto.getAmount()) {
			throw new IllegalArgumentException("参数不能为空");
		}
	}

	@Deprecated
	private boolean checkIsProduceDetail(AssetOperationDto dto) {

		AccountChange accountChange = getChangeByAmount(dto.getAmount().add(dto.getLockAmount()));
		if (accountChange == null || accountChange.equals(AccountChange.UNKNOWN)) {
			return false;
		}

		if (dto.getBusinessSubject().equals(BusinessSubject.UNLOCK)
				|| dto.getBusinessSubject().equals(BusinessSubject.LOCK)) {
			return false;
		}

		return true;

	}

	@Override
	public PageInfo<Finance> queryFinanceByBroker(Integer brokerId, Integer uid, Integer pageSize, Integer pageNo) {
		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy("uid asc");
		return new PageInfo<>(financeMapper.selectByBrokerId(brokerId, uid));
	}

	/**
	 * 核算资产
	 * @param ops
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateUserFinanceList(List<AssetOperationDto> ops) {
		if (null == ops || ops.isEmpty()) {
			throw new AppException(CommonCodeConst.SERVICE_ERROR, "传入ops为空");
		}

		// 对用户分组并排序
		Map<Integer, List<AssetOperationDto>> map = ops.stream()
				.collect(Collectors.groupingBy(AssetOperationDto::getUid));

		List<Finance> userFinanceList = new ArrayList<>();

		List<FinanceDetail> financeDetails = new ArrayList<>();

		Iterator<Integer> iter = map.keySet().stream().sorted().iterator();
		while (iter.hasNext()) {
			int uid = iter.next();
			List<AssetOperationDto> assetOperationDtos = map.get(uid);
			Map<String, Finance> userFinance = new HashMap<>();
            //对AssetOperationDto的index按照顺序排序
			List<AssetOperationDto> sortAssetOperationDtos = assetOperationDtos.stream()
					.sorted(new Comparator<AssetOperationDto>() {

						@Override
						public int compare(AssetOperationDto o1, AssetOperationDto o2) {

							return Integer.compare(o1.getIndex(), o2.getIndex());
						}
					}).collect(Collectors.toList());
           //对assetcode按照字典顺序排序
			assetOperationDtos.stream().map(a -> a.getAssetCode()).distinct().sorted().forEach(new Consumer<String>() {
				@Override
				public void accept(String assetCode) {
					Finance finane = null;
					finane = financeMapper.selectByUidAndAssetCode(uid, assetCode);
					if (null == finane) {
						throw new AppException(CommonCodeConst.SERVICE_ERROR, "用户资产代码:" + assetCode + "不存在");
					}
					finane = financeMapper.selectForUpdate(finane.getId());
					userFinance.put(assetCode, finane);
					userFinanceList.add(finane);
				}
			});

			for (AssetOperationDto assetOperationDto : sortAssetOperationDtos) {
				String assetCode = assetOperationDto.getAssetCode();

				Finance finane = userFinance.get(assetCode);
				// 加减资产

				BigDecimal amountAvailable = BigDecimalUtils.add(finane.getAmountAvailable(),
						assetOperationDto.getAmount());
				//log.info("uid:"+assetOperationDto.getUid()+"加减资产"+assetOperationDto.getAssetCode()+" 可用金额:" + finane.getAmountAvailable() + " " + assetOperationDto.getAmount()+" 冻结金额:" + finane.getAmountLock() + " " + assetOperationDto.getLockAmount());
				if (!BigDecimalUtils.isBiggerOrEqual(amountAvailable, BigDecimal.ZERO)) {
					throw new AppException(UserAssetCodeConst.COIN_ASSERT_LESS);
				}
				// 校验扣减后的资产
				BigDecimal amountLock = BigDecimalUtils.add(finane.getAmountLock(), assetOperationDto.getLockAmount());
				if (!BigDecimalUtils.isBiggerOrEqual(amountLock, BigDecimal.ZERO)) {
					log.error("解锁用户资产错误,用户lock不足financeid:{}", finane.getId());
					throw new AppException(CommonCodeConst.SERVICE_ERROR, "用户扣减lock错误");
				}

				BigDecimal amountLoan = BigDecimalUtils.add(finane.getAmountLoan(), assetOperationDto.getLoanAmount());
				if (!BigDecimalUtils.isBiggerOrEqual(amountLoan, BigDecimal.ZERO)) {
					throw new AppException(CommonCodeConst.SERVICE_ERROR, "用户扣减loan错误");
				}


				// 创建financedetail
				FinanceDetail financeDetail = new FinanceDetail();
				financeDetail.setAmountAvailable(assetOperationDto.getAmount());
				financeDetail.setAmountLoan(assetOperationDto.getLoanAmount());
				financeDetail.setAmountLock(assetOperationDto.getLockAmount());

				financeDetail.setBalanceOldAvailable(finane.getAmountAvailable());
				financeDetail.setBalanceOldLoan(finane.getAmountLoan());
				financeDetail.setBalanceOldLock(finane.getAmountLock());

				financeDetail.setBalanceNewAvailable(amountAvailable);
				financeDetail.setBalanceNewLoan(amountLoan);
				financeDetail.setBalanceNewLock(amountLock);
				financeDetail.setBusinessSubject(assetOperationDto.getBusinessSubject().name());
				financeDetail.setRequestNo(assetOperationDto.getRequestNo());
				financeDetail.setAssetCode(assetCode);
				financeDetail.setCreateDate(new Date());
				financeDetail.setUid(uid);

				if (assetOperationDto.getBusinessSubject().equals(BusinessSubject.LOCK)
						|| assetOperationDto.getBusinessSubject().equals(BusinessSubject.UNLOCK)) {
					financeDetail.setAssetChangeType(0);
				} else {
					financeDetail.setAssetChangeType(1);
				}
				// 更新finance
				finane.setAmountLoan(amountLoan);
				finane.setAmountLock(amountLock);
				finane.setAmountAvailable(amountAvailable);
				finane.setVersion(finane.getVersion() + 1);
				financeDetails.add(financeDetail);
			}

		}

		try {
			// 批量插入
			financeDetailMapper.insertBatch(financeDetails);
			// 批量更新
			//log.info("=====================================结算=======================================");
			for (Finance finance : userFinanceList) {
				//log.info("uid:"+finance.getUid()+"加减资产"+finance.getAssetCode()+" 可用金额:" + finance.getAmountAvailable() + "  冻结金额:" + finance.getAmountLock());
				financeMapper.updateByPrimaryKey(finance);
			}
			//log.info("=====================================结算=======================================");
		} catch (DuplicateKeyException e) {
			log.error(e.getMessage(),e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return;
		}
	}

	@Override
	public List<StatisticeResult> queryTotalCountByAssetCode(String assetCode) {
		return financeMapper.queryTotalCountByAssetCode(assetCode);
	}

	@Override
	public List<WalletBalance> queryWalletBalance(String type,String assetCode, Date date, String account,
																										Integer pageNo, Integer pageSize) {
		if (pageSize != -1){
			PageHelper.startPage(pageNo, pageSize);
		}
		String apiUrl =ResourceUtils.get("api", "api.wallet.url");
		String uri = "/account/dailyBalance";
		return getWalletBalance(apiUrl,uri,assetCode,date,account,type,pageNo,pageSize);
	}

	private List<WalletBalance> getWalletBalance(String apiUrl, String uri, String assetCode, Date date, String account,String type,Integer pageNo,Integer pageSize) {
		String reportDate;
		if (date == null || date.compareTo(new Date()) >= 0) {
			reportDate = DateUtils.formatDate(new Date()).split(" ")[0];
		}
		else {
			reportDate = DateUtils.formatDate(date).split(" ")[0];
		}
		String base = apiUrl+uri;
		Map<String,String> param = new HashMap<>();
		param.put("&assetCode=",assetCode);
		param.put("&date=",reportDate);
		param.put("&account=",account);
		param.put("&walletType=",type);
		if (param!=null){
			base = base + "?";
			Set<String> set = param.keySet();
			for (String key:set){
				base = base + key + param.get(key);
			}
			base = base.replace("?&","?");
		}
		try {
			String response = HttpUtil.get(base);
			WalletResponseBase responseBase = JSONObject.toJavaObject(JSON.parseObject(response), WalletResponseBase.class);
			List<WalletBalance> list = responseBase.getDataList().toJavaList(WalletBalance.class);
			if (responseBase.getRepCode() == 200) {
				return list;
			}else{
				log.error("钱包服务错误：{}", response);
				throw new AppException(CommonCodeConst.SERVICE_ERROR);
			}
		}catch (Exception e){
			log.error("钱包服务错误：{}", base);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public List<WalletBalanceTotal> queryWalletTotal(String assetCode,Date date, Integer pageNo, Integer pageSize) {
		if (pageSize != -1){
			PageHelper.startPage(pageNo, pageSize);
		}
		String apiUrl =ResourceUtils.get("api", "api.wallet.url");
		String uri = "/account/dailyBalance";
		List<WalletBalance> list = getWalletBalance(apiUrl, uri, assetCode, date, "", "",pageNo,pageSize);
		return bulidWalletBalanceTotal(list);
	}

	private List<WalletBalanceTotal> bulidWalletBalanceTotal(List<WalletBalance> list) {
		Map<String,List<WalletBalance>>map = list.stream().collect(Collectors.groupingBy(a -> a.getAssetCode()));
		List<WalletBalanceTotal> totals = new ArrayList<>();
		for (String key:map.keySet()){
			WalletBalanceTotal record = new WalletBalanceTotal();
			record.setAssetCode(key);
			record.setHotBalance(coutBalance("HOT",key,map));
			record.setColdBalance(coutBalance("COLD",key,map));
			record.setTotalBalance(record.getColdBalance().add(record.getHotBalance()));
			totals.add(record);
		}
		return totals;
	}

	private BigDecimal coutBalance(String type,String key, Map<String, List<WalletBalance>> map) {
		List<WalletBalance> list = map.get(key).stream().filter(a->a.getWalletType().equalsIgnoreCase(type)).collect(Collectors.toList());
		BigDecimal balances =  BigDecimal.ZERO;
		for (WalletBalance w:list){
			balances = balances.add(w.getBalance());
		}
		return balances;
	}

	@Override
	public List<WalletInOut> queryWalletInOut(Date beginDate,Date endDate, String opt, String assetCode, String account, Integer
			pageNo, Integer pageSize) {
		if (pageSize != -1){
			PageHelper.startPage(pageNo, pageSize);
		}
		String apiUrl =ResourceUtils.get("api", "api.wallet.url");
		String txn = "/txn/fullRecords";
		WalletInOutQueryRequest request = WalletInOutQueryRequest.builder()
																														 .beginDate(DateUtils.formatDate(beginDate).split(" ")[0])
																														 .assetCode(assetCode)
																														 .endDate(DateUtils.formatDate(endDate).split(" ")[0])
																														 .opt(opt.equalsIgnoreCase("")?"ALL":opt)
																														 .account(account)
																														 .page(pageNo)
																														 .pageSize(pageSize).build();
		return getWalletInOut(apiUrl,txn,request);
	}

	private List<WalletInOut> getWalletInOut(String apiUrl,String uri,WalletInOutQueryRequest walletInOutQueryRequest) {
		try {
			String response =
					com.gop.util.HttpUtil.post(apiUrl + uri, JSONObject.toJSONString(walletInOutQueryRequest), "UTF-8", 30000);
      WalletResponseBase responseBase = JSONObject.toJavaObject(JSON.parseObject(response), WalletResponseBase.class);
      if (responseBase.getRepCode() == 200) {
        return responseBase.getDataList().toJavaList(WalletInOut.class);
      } else {
				log.error("钱包服务错误：{}", response);
				throw new AppException(CommonCodeConst.SERVICE_ERROR);
			}
		}catch (Exception e){
			log.error("钱包服务错误：{}", walletInOutQueryRequest);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
	}

	@Override
	public List<WalletInOutTotal> queryWalletInOutTotal(String assetCode,Date beginDate, Date endDate, Integer pageNo, Integer pageSize) {
		if (pageSize != -1){
			PageHelper.startPage(pageNo, pageSize);
		}
		String apiUrl =ResourceUtils.get("api", "api.wallet.url");
		String txn = "/txn/fullRecords";
		String dailyBalance = "/account/dailyBalance";
		List<WalletBalance> balanceList = getWalletBalance(apiUrl,dailyBalance,assetCode,beginDate,"","",pageNo,pageSize);
		if (balanceList == null){
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		WalletInOutQueryRequest request = WalletInOutQueryRequest.builder()
																														 .beginDate(DateUtils.formatDate(beginDate).split(" ")[0])
																														 .endDate(DateUtils.formatDate(endDate).split(" ")[0])
																														 .assetCode(assetCode)
                                                             .opt("ALL")
																														 .build();
		List<WalletInOut> inOutList = getWalletInOut(apiUrl,txn,request);
		return bulidWalletInOutTotal(balanceList,inOutList);
	}

	private List<WalletInOutTotal> bulidWalletInOutTotal(List<WalletBalance> balanceList,List<WalletInOut> inOutList) {
		List<WalletBalanceTotal> walletBalanceTotals = bulidWalletBalanceTotal(balanceList);
		Map<String,BigDecimal> preAmountMap = new HashMap<>();
		for(WalletBalanceTotal w:walletBalanceTotals){
			preAmountMap.put(w.getAssetCode(),w.getColdBalance().add(w.getHotBalance()));
		}

		Map<String,List<WalletInOut>>map = inOutList.stream().collect(Collectors.groupingBy(a -> a.getAssetCode()));
		List<WalletInOutTotal> totals = new ArrayList<>();
		for (String key:map.keySet()){
			WalletInOutTotal record = new WalletInOutTotal();
			record.setAssetCode(key);
			List<WalletInOut> inList = map.get(key).stream().filter(a->a.getOpt().equalsIgnoreCase("IN")).collect(Collectors.toList());
			List<WalletInOut> outList = map.get(key).stream().filter(a->a.getOpt().equalsIgnoreCase("OUT")).collect(Collectors.toList());
			BigDecimal inAmount =  BigDecimal.ZERO;
			BigDecimal outAmount =  BigDecimal.ZERO;
			for (WalletInOut w:inList){
				inAmount = inAmount.add(w.getAmount());
			}
			for (WalletInOut w:outList){
				outAmount = outAmount.add(w.getAmount());
			}
			record.setDepositAmount(inAmount);
			record.setWithDrawAmount(outAmount);
			record.setPreAmount(preAmountMap.get(key)==null?BigDecimal.ZERO:preAmountMap.get(key));
			record.setTotalAmount(inAmount.add(outAmount).add(record.getPreAmount()));
			totals.add(record);
		}
		return totals;
	}

	@Override
	public PageInfo<FinanceAmountDto> getTotalAccountByAssetCode(String assetCode, Integer pageNo,Integer pageSize) {
		PageHelper.startPage(pageNo,pageSize);
		return new PageInfo<>(financeMapper.getTotalAccountByAssetCode(assetCode));
	}

	@Override
	public List<FinanceAmountDto> getTotalAccountNoPage(String assetCode) {
		return financeMapper.getTotalAccountByAssetCode(assetCode);
	}

	@Override
	public PageInfo<Finance> selectUserAccountList(Integer uid, String assetCode, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo,pageSize);
		return new PageInfo<>(financeMapper.selectUserAccountList(uid,assetCode));
	}

	@Override
	public List<Finance> getUserAccountList(Integer uid, String assetCode) {
		return financeMapper.selectUserAccountList(uid,assetCode);
	}
	@Override
	public List<Finance> selectUserAccountListByCurrencyType(Integer uid, CurrencyType currencyType) {
		return financeMapper.selectUserAccountListByCurrencyType(uid,currencyType);
	}

	@Override
	public void sendEmailReport(String assetCode, Date beginDate, Date endDate) {
		List<WalletBalanceTotal> walletBalanceTotal = queryWalletTotal(assetCode, endDate, 1, -1);
		List<WalletInOutTotal> walletInOutTotal = queryWalletInOutTotal(assetCode, beginDate, endDate, 1, -1);
		PageInfo<BeginingBalance>
				assetTotal = beginingBalanceService.queryBeginingBalance(assetCode.equalsIgnoreCase("") ? null : assetCode, endDate, 1, 10000);
		List<PlatAssetProcess> assetFlowTotal = buildAssetsFlowTotal(assetCode.equalsIgnoreCase("") ? null : assetCode, beginDate, endDate, 1, 10000);
		PageInfo<FinanceTotal> list = countFinanceTotal(walletBalanceTotal, walletInOutTotal, assetTotal.getList(), assetFlowTotal);

		Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
		List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
		for(FinanceTotal financeTotal : list.getList()){
			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("assetCode",financeTotal.getAssetCode());
			resMap.put("inoutBalance",financeTotal.getInoutBalance());
			resMap.put("walletBalance",financeTotal.getWalletBalance());
			resMap.put("walletDiff",financeTotal.getWalletDiff());
			resMap.put("platWalletDiff",financeTotal.getPlatWalletDiff());
			resMap.put("platDiff",financeTotal.getPlatDiff());
			resMap.put("assetBalance",financeTotal.getAssetBalance());
			resMap.put("assetProcess",financeTotal.getDepositWithdrawBalance());
			lst.add(resMap);
		}
		resultMerStlMap.put("resultList", lst);
      exportExcelService.createTemplateXlsByFileName(TOTAL_FTL,resultMerStlMap, rootPath+TOTAL_NAME);
		sendEmail();
	}
	private PageInfo<FinanceTotal> countFinanceTotal(List<WalletBalanceTotal> walletBalanceTotals, List<WalletInOutTotal> walletInOutTotals,
																									 List<BeginingBalance> financeAmount, List<PlatAssetProcess> platAssetProcess) {
		Map<String,BigDecimal> balanceMap = new HashMap<>();
		Map<String,BigDecimal> inoutMap = new HashMap<>();
		Map<String,BigDecimal> assetMap = new HashMap<>();
		Map<String,BigDecimal> processMap = new HashMap<>();
		walletBalanceTotals.forEach(balance -> balanceMap.put(balance.getAssetCode(),balance.getTotalBalance()));
		walletInOutTotals.forEach(inout -> inoutMap.put(inout.getAssetCode(),inout.getTotalAmount()));
		financeAmount.forEach(asset -> assetMap.put(asset.getAssetCode(),asset.getAmount_total()));
		platAssetProcess.forEach(process -> processMap.put(process.getAssetCode(),process.getCulBalance()));

		List<Map<String,BigDecimal>> listMap = new ArrayList<>();
		listMap.add(balanceMap);
		listMap.add(inoutMap);
		listMap.add(assetMap);
		listMap.add(processMap);
		Collections.sort(listMap, new Comparator<Map<String, BigDecimal>>() {
			@Override
			public int compare(Map<String, BigDecimal> o1, Map<String, BigDecimal> o2) {
				return o2.size() - o1.size();
			}
		});

		List<FinanceTotal> list = new ArrayList<>();
		for (String key: listMap.get(0).keySet()){
			FinanceTotal total = new FinanceTotal();
			total.setAssetCode(key);
			total.setWalletBalance(balanceMap.get(key)==null?BigDecimal.ZERO:balanceMap.get(key));
			total.setInoutBalance(inoutMap.get(key)==null?BigDecimal.ZERO:inoutMap.get(key));
			total.setAssetBalance(assetMap.get(key)==null?BigDecimal.ZERO:assetMap.get(key));
			total.setDepositWithdrawBalance(processMap.get(key)==null?BigDecimal.ZERO:processMap.get(key));
			BigDecimal walletDiff =total.getInoutBalance().subtract(total.getWalletBalance());
			BigDecimal platDiff = total.getAssetBalance().subtract(total.getDepositWithdrawBalance());
			BigDecimal platWalletDiff = walletDiff.subtract(platDiff);
			total.setPlatDiff(platDiff);
			total.setWalletDiff(walletDiff);
			total.setPlatWalletDiff(platWalletDiff);
			list.add(total);
		}
		return new PageInfo<>(list);
	}

	private List<PlatAssetProcess> buildAssetsFlowTotal(String assetCode,Date beginDate,Date endDate,Integer pageNo,Integer pageSize) {

		List<PlatAssetProcess> platAssetProcessDtoList = Lists.newArrayList();
		try {
			endDate = DateUtils.parseDate(DateFormatUtils.format(endDate, "yyyy-MM-dd") + " 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<PlatAssetProcess> platAssetProcesses = platAssetProessMapper.selectList(assetCode,endDate);
		for(PlatAssetProcess process :platAssetProcesses){
			PlatAssetProcess platAssetProcess = new PlatAssetProcess();
			platAssetProcess.setAssetCode(process.getAssetCode());
			platAssetProcess.setBeginBalance(process.getBeginBalance());
			platAssetProcess.setDepositBalance(process.getDepositBalance());
			platAssetProcess.setWithdrawTotal(process.getWithdrawTotal());
			platAssetProcess.setWithdrawUnknow(process.getWithdrawUnknow());
			platAssetProcess.setWithdrawRefuse(process.getWithdrawRefuse());
			platAssetProcess.setWithdrawSuccess(process.getWithdrawSuccess());
			platAssetProcess.setWithdrawFee(process.getWithdrawFee());
			platAssetProcess.setBrokenAssetBalance(process.getBrokenAssetBalance());
			platAssetProcess.setTradeFee(process.getTradeFee());
			platAssetProcess.setOther(process.getOther());
			platAssetProcess.setEndBalance(process.getEndBalance());
			platAssetProcess.setCulBalance(process.getCulBalance());
			platAssetProcessDtoList.add(platAssetProcess);
		}
		return platAssetProcessDtoList;
	}
	@Transactional
	public void sendEmail(){
		List<File> files = Lists.newArrayList(new File(rootPath+TOTAL_NAME));  //增加附加列表
		//发送邮件
		EmailDto email = EmailDto.builder().toUser(Arrays.asList(toUser.split(","))).subject(subject).text(text).fileList(files).build();
		Boolean isSent = iEmailService.sendAttachmentsMail(email);
		buildEmailLog(new Date(),"财务日报表",isSent);
	}

	private void buildEmailLog(Date reportDate,String content, Boolean isSent) {
		EmailLog emailLog = new EmailLog();
		emailLog.setMsgId("");// msgId 短信服务商返回的信息；
		emailLog.setSysCode(SysCode.GTE_MANAGER);
		emailLog.setServiceCode(ServiceCode.REPORT_DAILY);
		emailLog.setServiceProvider(ServiceProvider.TENCENT);
		emailLog.setMsgContent(reportDate +" "+content +" "+ (isSent?"发送成功":"发送失败"));
		emailLog.setEmail(toUser); // 需要传送 邮箱发送地址
		emailLog.setCreateDate(new Date());
		emailLogService.addEmailLog(emailLog);
	}
}
