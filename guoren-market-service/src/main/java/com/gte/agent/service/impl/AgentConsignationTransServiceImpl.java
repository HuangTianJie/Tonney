package com.gte.agent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gte.agent.service.AgentConsignationTransService;
import com.gte.mapper.agent.AgentTradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AgentConsignationTransServiceImpl implements AgentConsignationTransService {

    @Resource
    private AgentTradeOrderMapper agentTradeOrderMapper;

    @Override
    public PageModel<TradeOrder> queryConsignation(Integer adminId, Integer uId, String symbol,
                                                   String outerOrderNo, TradeCoinType orderType, TradeCoinStatus status, Date startDate,
                                                   Date endDate, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize, true);
        List<TradeOrder> tradeOrders = new ArrayList<>();
        try {
            tradeOrders = agentTradeOrderMapper.queryConsignation(adminId, uId, symbol, outerOrderNo, orderType, status, startDate, endDate);
        } catch (Exception e) {
            log.error("查询数据库订单表异常,error:{}", e);
            throw new AppException(CommonCodeConst.SERVICE_ERROR);
        }
        if (null == tradeOrders || tradeOrders.isEmpty()) {
            return null;
        }
        Page<TradeOrder> page = (Page<TradeOrder>) tradeOrders;
        PageModel<TradeOrder> pageModel = new PageModel<>();
        pageModel.setList(tradeOrders);
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(page.getPages());
        pageModel.setPageSize(page.getPageSize());
        return pageModel;
    }

    @Override
    public int querytotal(Integer adminId) {
        return agentTradeOrderMapper.querytotal(adminId);
    }

    @Override
    public int querytotaldealed(Integer adminId) {
        return agentTradeOrderMapper.querytotaldealed(adminId);
    }

    @Override
    public int querytotaldealing(Integer adminId) {
        return agentTradeOrderMapper.querytotaldealing(adminId);
    }
}
