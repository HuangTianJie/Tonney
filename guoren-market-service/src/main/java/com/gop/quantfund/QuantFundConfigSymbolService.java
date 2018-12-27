package com.gop.quantfund;

import com.gop.domain.QuantFundConfigSymbol;
import com.gop.domain.enums.QuantFundConfigSymbolStatus;
import com.gop.match.dto.MatchOrderDto;
import com.gop.mode.vo.PageModel;

public interface QuantFundConfigSymbolService {

	public int createOrUpdate(QuantFundConfigSymbol symbol);
	
	public QuantFundConfigSymbol selectBySymbol(String symbol);
	
	public void checkQuantFundConfigSymbolOrder(String symbol,MatchOrderDto matchOrderDto);

	public PageModel<QuantFundConfigSymbol> getConfigSymbolPage(QuantFundConfigSymbolStatus status,
			String fundSymbol, Integer pageSize, Integer pageNo);

}
