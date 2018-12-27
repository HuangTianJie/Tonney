package com.gop.quantfund;

import java.util.List;

import com.gop.domain.QuantFundConfig;
import com.gop.domain.enums.QuantFundConfigType;
import com.gop.mode.vo.PageModel;

public interface QuantFundConfigService {
	public QuantFundConfig getConfigValueByCodeAndKey(String fundCode, QuantFundConfigType key);

	public int createOrUpdate(QuantFundConfig quantFundConfig);

	public PageModel<QuantFundConfig> getConfigPageByKey(QuantFundConfigType key,String fundcode, Integer pageSize, Integer pageNo);
	
	List<QuantFundConfig> queryConfigByCode(String fundCode);
}
