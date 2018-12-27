package com.gop.coin.transfer.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.code.consts.CommonCodeConst;
import com.gop.coin.transfer.service.ChannelCoinAddressDepositInfoService;
import com.gop.coin.transfer.service.DepositCoinAddressService;
import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.ChannelCoinAddressDepositInfo;
import com.gop.domain.ChannelCoinAddressDepositPool;
import com.gop.domain.enums.CoinAddressStatus;
import com.gop.domain.enums.DelFlag;
import com.gop.domain.enums.InnerAddressFlag;
import com.gop.exception.AppException;
import com.gop.mapper.ChannelCoinAddressDepositMapper;
import com.gop.mapper.ChannelCoinAddressDepositPoolMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DepositCoinAddressServiceImpl implements DepositCoinAddressService {

	@Autowired
	private ChannelCoinAddressDepositMapper channelCoinAddressDepositMapper;

	@Autowired
	private ChannelCoinAddressDepositPoolMapper channelCoinAddressDepositPoolMapper;
	
	@Autowired
	private ChannelCoinAddressDepositInfoService channelCoinAddressDepositInfoService;

	@Override
	public ChannelCoinAddressDeposit getCoinDepositAddress(String assetCode, int uid) {
		ChannelCoinAddressDeposit address = channelCoinAddressDepositMapper.getAddress(uid, assetCode);
		if (address == null) {
			createCoinAddress(assetCode, uid, assetCode);
			return address;
		}
		return address;
	}

	@Override
	public List<ChannelCoinAddressDeposit> getCoinDepositAddressList(String assetCode, int uid) {

		List<ChannelCoinAddressDeposit> lst = channelCoinAddressDepositMapper.getAddressList(uid, assetCode);
		if (lst == null || lst.isEmpty()) {
			return null;
		}
		return lst;
	}

	@Override
	public ChannelCoinAddressDeposit getCoinDepositAddress(String address, String assetCode) {
		return channelCoinAddressDepositMapper.selectByAddressAndAssetCodeNotDel(address, assetCode);
	}

	@Override
	public InnerAddressFlag checkIsInnerAddress(String address, String assetCode) {

		return null == getCoinDepositAddress(address, assetCode) ? InnerAddressFlag.NO : InnerAddressFlag.YES;

	}

	@Override
	public boolean updateAddress(ChannelCoinAddressDeposit address) {
		return channelCoinAddressDepositMapper.updateByPrimaryKey(address) > 0 ? true : false;
	}

	@Override
	public boolean addAddress(ChannelCoinAddressDeposit address) {
		return channelCoinAddressDepositMapper.insert(address) > 0 ? true : false;
	}

	@Override
	@Transactional
	public boolean createCoinAddress(String assetCode, int uid, String memo) {
		ChannelCoinAddressDeposit userCoinAddress = new ChannelCoinAddressDeposit();
		ChannelCoinAddressDepositPool address = null;
		Boolean isErc20 = false;
		Boolean isErc20Used = false;
		while (true) {
			ChannelCoinAddressDepositInfo channelCoinAddressDepositInfo = channelCoinAddressDepositInfoService
					.getChannelCoinAddressDepositInfoByTargetAssetCode(assetCode);
			String adressAssetCode = "";
			//判断是否为ERC20代币
			if (null != channelCoinAddressDepositInfo) {
				adressAssetCode = channelCoinAddressDepositInfo.getAddressAssetCode();
				address = channelCoinAddressDepositPoolMapper.selectByUidAndAssetCode(uid,adressAssetCode);
				isErc20 = true;
				//判断是否为第一次分配ERC20代币地址
				if (null != address) {
					isErc20Used = true;
				}
			}
			if (address == null) {
				//若为ERC20代币，根据ERC20查询有效未分配地址，否则按原币种名称查询
				if (isErc20) {
					address = channelCoinAddressDepositPoolMapper.selectUsefulAddress(adressAssetCode);
				}else {
					address = channelCoinAddressDepositPoolMapper.selectUsefulAddress(assetCode);
				}
			} 
			if (address == null) {
				return false;
			} 
			//若为非第一次分配ERC20地址，无需对地址进行锁操作，无需考虑并发抢地址问题
			if (isErc20Used) {
				break;
			}
			address = channelCoinAddressDepositPoolMapper.selectForUpdate(address.getId());
			
			if (address.getDelFlag().equals(DelFlag.TRUE)
					|| address.getAddressStatus().equals(CoinAddressStatus.USED)) {

			} else {
				break;
			}
		}

		address.setAddressStatus(CoinAddressStatus.USED);
		address.setUid(uid);
		address.setUpdateDate(new Date());
		channelCoinAddressDepositPoolMapper.updateByPrimaryKey(address);

		userCoinAddress.setCoinAddress(address.getCoinAddress());
		userCoinAddress.setAssetCode(assetCode);
		userCoinAddress.setCreateDate(new Date());
		userCoinAddress.setCreateIp("");
		userCoinAddress.setDelFlag(DelFlag.FALSE);
		userCoinAddress.setDepositPoolId(address.getId());
		userCoinAddress.setName(address.getName());
		userCoinAddress.setUid(uid);
		addAddress(userCoinAddress);
		return true;
	}

}
