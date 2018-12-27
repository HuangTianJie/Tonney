package com.gop.coin.transfer.service;

import java.util.List;

import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.enums.InnerAddressFlag;

public interface DepositCoinAddressService {

	public ChannelCoinAddressDeposit getCoinDepositAddress(String assetCode, int uid);

	public ChannelCoinAddressDeposit getCoinDepositAddress(String address, String assetCode);

	public List<ChannelCoinAddressDeposit> getCoinDepositAddressList(String assetCode, int uid);

	public boolean updateAddress(ChannelCoinAddressDeposit address);

	public boolean addAddress(ChannelCoinAddressDeposit address);

	public InnerAddressFlag checkIsInnerAddress(String address, String assetCode);

	public boolean createCoinAddress(String assetCode, int uid, String memo);
}
