package com.gte.collection.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.response.CollectionAccountResponse;
import com.gop.mapper.ChannelCoinAddressDepositMapper;
import com.gte.collection.service.CoinCollectionService;
import com.gte.domain.dto.FundAcctBalDto;
import com.gte.mapper.collection.FundAcctBalMapper;
@Service
public class CoinCollectionServiceImpl implements CoinCollectionService{
	@Autowired
   private ChannelCoinAddressDepositMapper channelCoinAddressDepositMapper;
	@Autowired
	   private FundAcctBalMapper fundAcctBalMapper;
	@Override
	public List<ChannelCoinAddressDeposit> getCollectionAddressList(
			String assetCode) {
		
		return channelCoinAddressDepositMapper.getCollectionAddressList(assetCode);
	}
	@Override
	public List<CollectionAccountResponse> getCollectionAccountList(
			String assetsCode) {
		List<CollectionAccountResponse> collectionAccountResponses = new ArrayList<CollectionAccountResponse>();
		FundAcctBalDto fundAcctBalDto = new FundAcctBalDto();
		fundAcctBalDto.setAssetsCode(assetsCode);
		List<FundAcctBalDto> fundAcctBalByAssetCode = fundAcctBalMapper.getFundAcctBalByAssetCode(fundAcctBalDto);
		for (int i = 0; i < fundAcctBalByAssetCode.size(); i++) {
			FundAcctBalDto fundAcctBalDto2 = fundAcctBalByAssetCode.get(i);
			CollectionAccountResponse collectionAccountResponse = new CollectionAccountResponse();
			collectionAccountResponse.setAssetsCode(fundAcctBalDto2.getAssetsCode());
			collectionAccountResponse.setAmountAvailable(fundAcctBalDto2.getAmountAvailable());
			collectionAccountResponse.setAmountLock(fundAcctBalDto2.getAmountLock());
			collectionAccountResponse.setAmountTotal(fundAcctBalDto2.getAmountTotal());
			collectionAccountResponses.add(collectionAccountResponse);
		}
		return collectionAccountResponses;
	}
   
}
