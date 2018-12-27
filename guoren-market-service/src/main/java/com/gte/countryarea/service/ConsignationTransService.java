package com.gte.countryarea.service;

import java.util.List;

import com.gte.mapper.dto.ConsignationTransRecord;

public interface ConsignationTransService {

	List<ConsignationTransRecord> getList(ConsignationTransRecord consignationTransRecord);
	
}
