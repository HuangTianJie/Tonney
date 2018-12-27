package com.gte.countryarea.service;


import com.gop.mode.vo.PageModel;
import com.gte.domain.CountryArea;

public interface CountryAreaService {

	PageModel<CountryArea> getList();

	void addCountry(CountryArea countryArea);

	void updateStatus(CountryArea countryArea);

}
