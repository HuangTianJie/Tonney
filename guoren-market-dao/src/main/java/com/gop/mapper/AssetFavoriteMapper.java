package com.gop.mapper;

import java.util.List;


import com.gop.domain.AssetFavorite;

public interface AssetFavoriteMapper {
	List<AssetFavorite> selectAllByUid(Integer uid);

	void insert(AssetFavorite assetFavorite);

	public AssetFavorite selectAllBySymbol(AssetFavorite assetFavorite);

}