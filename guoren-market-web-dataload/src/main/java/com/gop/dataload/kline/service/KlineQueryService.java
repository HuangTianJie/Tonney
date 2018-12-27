package com.gop.dataload.kline.service;

import com.gop.dataload.kline.dto.KlineQueryDto;
import com.gop.dataload.kline.param.KlineQueryPages;

public interface KlineQueryService {

	KlineQueryPages getKlinePages(KlineQueryDto dto);

}
