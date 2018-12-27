package com.gop.coin.transfer.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.gop.domain.StatisticeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.coin.transfer.service.DepositCoinQueryService;
import com.gop.domain.DepositCoinOrderUser;
import com.gop.domain.enums.DepositCoinAssetStatus;
import com.gop.mapper.DepositCoinOrderUserMapper;

@Service("depositCoinService")
public class DepositCoinQueryServiceImpl implements DepositCoinQueryService {
	@Autowired
	private DepositCoinOrderUserMapper depositCoinOrderUserMapper;

	@Override
	public PageInfo<DepositCoinOrderUser> queryOrder(Integer brokerId, Integer id, String account, Integer uId,
			String address, String txid, String assetCode,Date beginTime,Date endTime ,String email,DepositCoinAssetStatus status, Integer pageNo,
			Integer pageSize) {

		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy("id desc");

		return new PageInfo<DepositCoinOrderUser>(depositCoinOrderUserMapper.selectDepositOrderList(brokerId, id, account, uId, address, txid,
				assetCode, beginTime,endTime,email,status));
	}

	@Override
	public List<DepositCoinOrderUser> queryOrder(Integer uId, Date fromDate, Date toDate) {
		return depositCoinOrderUserMapper.selectByUidAndDateScope(uId, fromDate, toDate);
	}

	@Override
	public PageInfo<DepositCoinOrderUser> getTransferList(int uid, String assetCode, Integer pageSize, Integer pageNo) {
		PageHelper.startPage(pageNo, pageSize);
		PageHelper.orderBy("update_date desc");
		return new PageInfo<>(depositCoinOrderUserMapper.selectListByUidAndAssetCode(uid, assetCode));
		
	}

	@Override
	public BigDecimal getTotalDeposit(String assetcode, DepositCoinAssetStatus status, Date startDate, Date endDate) {
		return depositCoinOrderUserMapper.getTotalDeposit(assetcode,status,startDate,endDate);
	}
	@Override
	public List<StatisticeResult> getTotalDeposits(String assetcode, DepositCoinAssetStatus status, Date startDate, Date endDate) {
		return depositCoinOrderUserMapper.getTotalDeposits(assetcode,status,startDate,endDate);
	}
	@Override
	public List<StatisticeResult> depositStaitstic(){
		return depositCoinOrderUserMapper.depositStaitstic();
	}
}
