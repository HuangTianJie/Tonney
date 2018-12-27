package com.gte.countryarea.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gte.countryarea.service.ConsignationTransService;
import com.gte.mapper.ConsignationTransRecordMapper;
import com.gte.mapper.dto.ConsignationTransRecord;

@Service
public class ConsignationTransServiceImpl implements ConsignationTransService{

	@Autowired
	private ConsignationTransRecordMapper consignationTransRecordMapper;
	@Override
	public List<ConsignationTransRecord> getList(ConsignationTransRecord consignationTransRecord) {
		return consignationTransRecordMapper.getList(consignationTransRecord);
	}
	
}
