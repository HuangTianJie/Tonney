package com.gop.asset.service;

import java.util.List;

import com.gop.domain.AssetFavorite;

public interface AssetFavoriteService {
	public List<AssetFavorite> selectAllByUid(Integer uid);
	
	public void insert(AssetFavorite assetFavorite);

	public AssetFavorite selectAllBySymbol(AssetFavorite assetFavorite);

}
