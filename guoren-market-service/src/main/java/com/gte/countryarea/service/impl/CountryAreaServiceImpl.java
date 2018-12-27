package com.gte.countryarea.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.mode.vo.PageModel;
import com.gte.countryarea.service.CountryAreaService;
import com.gte.domain.CountryArea;
import com.gte.mapper.CountryAreaMapper;

@Service
public class CountryAreaServiceImpl implements CountryAreaService{
	@Autowired
	private CountryAreaMapper countryAreaMapper;
	
	@Override
	public PageModel<CountryArea> getList() {
		
		List<CountryArea> tradeOrders = countryAreaMapper.getList();
		PageModel<CountryArea> pageModel = new PageModel<>();
		pageModel.setList(tradeOrders);
		return pageModel;
		
	}

	@Override
	public void addCountry(CountryArea countryArea) {
		 countryAreaMapper.addCountry(countryArea);
	}

	@Override
	public void updateStatus(CountryArea countryArea) {
		countryAreaMapper.updateStatus(countryArea);		
	}

}
