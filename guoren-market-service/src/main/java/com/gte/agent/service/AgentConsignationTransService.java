package com.gte.agent.service;

import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import com.gop.mode.vo.PageModel;

import java.util.Date;

public interface AgentConsignationTransService {

    PageModel<TradeOrder> queryConsignation(Integer adminId, Integer uId, String symbol,
                                                   String orderNo, TradeCoinType tradeCoinType, TradeCoinStatus status, Date startDate,
                                                   Date endDate, Integer pageNo, Integer pageSize);

    int querytotal(Integer adminId);

    int querytotaldealed(Integer adminId);

    int querytotaldealing(Integer adminId);
}
