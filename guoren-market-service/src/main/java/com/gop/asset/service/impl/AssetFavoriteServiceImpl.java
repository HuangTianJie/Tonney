package com.gop.asset.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.asset.service.AssetFavoriteService;
import com.gop.domain.AssetFavorite;
import com.gop.mapper.AssetFavoriteMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssetFavoriteServiceImpl implements AssetFavoriteService {

	@Autowired
	private AssetFavoriteMapper assetFavoriteMapper;

	@Override
	public List<AssetFavorite> selectAllByUid(Integer uid) {

		return assetFavoriteMapper.selectAllByUid(uid);
	}


	@Override
	public void insert(AssetFavorite assetFavorite) {
			assetFavoriteMapper.insert(assetFavorite);
	}

	@Override
	public AssetFavorite selectAllBySymbol(AssetFavorite assetFavorite) {
		return assetFavoriteMapper.selectAllBySymbol(assetFavorite);
	}

}
