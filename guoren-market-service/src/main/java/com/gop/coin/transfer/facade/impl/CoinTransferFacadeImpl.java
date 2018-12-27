package com.gop.coin.transfer.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.coin.transfer.facade.CoinTransferFacade;
import com.gop.coin.transfer.service.DepositCoinAddressService;

@Service
public class CoinTransferFacadeImpl implements CoinTransferFacade {

	@Autowired
	private DepositCoinAddressService depositCoinAddressService;

	@Override
	public void createCoinAddress(String assetCode, int uid, String memo) {

		depositCoinAddressService.createCoinAddress(assetCode, uid, memo);
	}

}
