package com.gte.collection.service;

import java.util.List;

import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.response.CollectionAccountResponse;

public interface CoinCollectionService {
   List<ChannelCoinAddressDeposit>  getCollectionAddressList(String assetCode);
   List<CollectionAccountResponse> getCollectionAccountList(String assetCode);
}
