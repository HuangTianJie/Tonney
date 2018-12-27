package com.gte.mapper;

import java.util.List;

import com.gte.domain.CountryArea;


public interface CountryAreaMapper {

	List<CountryArea> getList();

	void addCountry(CountryArea countryArea);

	void updateStatus(CountryArea countryArea);
}
