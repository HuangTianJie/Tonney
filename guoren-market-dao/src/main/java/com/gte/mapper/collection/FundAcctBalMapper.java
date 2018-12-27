package com.gte.mapper.collection;

import java.util.List;

import com.gte.domain.dto.FundAcctBalDto;

public interface FundAcctBalMapper {
	public List<FundAcctBalDto> getFundAcctBalByAssetCode(FundAcctBalDto lundAcctBalDto);

}
