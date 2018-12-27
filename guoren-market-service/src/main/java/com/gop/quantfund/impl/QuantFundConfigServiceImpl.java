package com.gop.quantfund.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.QuantFundConfig;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.exception.AppException;
import com.gop.mapper.QuantFundConfigMapper;
import com.gop.mode.vo.PageModel;
import com.gop.quantfund.QuantFundConfigService;

import lombok.extern.slf4j.Slf4j;

@Service("QuantFundConfigService")
@Slf4j
public class QuantFundConfigServiceImpl implements QuantFundConfigService {
	@Autowired
	private QuantFundConfigMapper quantFundConfigMapper;

	@Override
	public QuantFundConfig getConfigValueByCodeAndKey(String fundCode, QuantFundConfigType key) {
		if (Strings.isNullOrEmpty(fundCode) || null == key) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		List<QuantFundConfig> list = quantFundConfigMapper.selectByCodeAndKey(fundCode, key);
		if (list.size() == 0) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		return list.get(0);
	}

	@Override
	public int createOrUpdate(QuantFundConfig quantFundConfig) {
		int result = quantFundConfigMapper.insertOrUpdate(quantFundConfig);
		if (result == 0) {
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		return result;
	}

	@Override
	public PageModel<QuantFundConfig> getConfigPageByKey(QuantFundConfigType key, String fundCode, Integer pageSize,
			Integer pageNo) {
		PageModel<QuantFundConfig> pageModel = new PageModel<>();
		PageInfo<QuantFundConfig> pageInfo = new PageInfo<>(quantFundConfigMapper.selectByCodeAndKey(fundCode, key));
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(pageInfo.getList());
		return pageModel;
	}

	@Override
	public List<QuantFundConfig> queryConfigByCode(String fundCode) {
		return quantFundConfigMapper.selectByCodeAndKey(fundCode, null);
	}

}
