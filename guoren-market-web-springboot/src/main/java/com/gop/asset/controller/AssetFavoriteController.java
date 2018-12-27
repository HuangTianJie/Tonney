package com.gop.asset.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gop.asset.service.AssetFavoriteService;
import com.gop.domain.AssetFavorite;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController
@RequestMapping("/asset-favorite")
public class AssetFavoriteController {

	@Autowired
	private AssetFavoriteService assetFavoriteService;

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/asset-favorite-list", method = RequestMethod.GET)
	public List<AssetFavorite> findList(@AuthForHeader AuthContext context) {
		Integer uid = context.getLoginSession().getUserId();
		List<AssetFavorite> list = assetFavoriteService.selectAllByUid(uid);
		return list;
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/asset-favorite-insert", method = RequestMethod.POST)
	public void insert(@AuthForHeader AuthContext context,@RequestBody AssetFavorite assetFavorite) {
		Integer uid = context.getLoginSession().getUserId();
		assetFavorite.setUid(uid);
		assetFavoriteService.insert(assetFavorite);
	}

	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
	@RequestMapping(value = "/asset-favorite-info", method = RequestMethod.POST)
	public AssetFavorite findInfo(@AuthForHeader AuthContext context,@RequestBody AssetFavorite assetFavorite) {
		Integer uid = context.getLoginSession().getUserId();
		assetFavorite.setUid(uid);
		AssetFavorite info = assetFavoriteService.selectAllBySymbol(assetFavorite);
		return info;
	}
}
