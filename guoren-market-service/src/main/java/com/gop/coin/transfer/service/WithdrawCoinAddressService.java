package com.gop.coin.transfer.service;

import com.github.pagehelper.PageInfo;
import com.gop.domain.ChannelCoinAddressWithdraw;

public interface WithdrawCoinAddressService {

	public PageInfo<ChannelCoinAddressWithdraw> getTransferList(int uid, String assetCode, int pageNo, int pageSize);

	public void addWithdrawCoinAddress(String address, String memo, int uid, String assetCode);

	public void deleteWithdrawCoinAddress(int addressId, int uid);

	public ChannelCoinAddressWithdraw getById(int id);

}
