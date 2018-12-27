package com.gte.consignationTrans.controller;

import com.google.gson.Gson;
import com.gop.common.CheckCodeService;
import com.gop.domain.TradeOrder;
import com.gop.domain.enums.TradeCoinStatus;
import com.gop.domain.enums.TradeCoinType;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.agent.service.AgentConsignationTransService;
import com.gte.consignationTrans.dto.MatchOrderDetail;
import com.gte.countryarea.service.ConsignationTransService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/agent/consignationTrans")
@Api("委托")
public class ConsignationTransController {


    @Autowired
    private Gson gson;

    @Autowired
    private ConsignationTransService consignationTransService;

    @Autowired
    AgentConsignationTransService agentConsignationTransService;

    @Autowired
    private CheckCodeService checkCodeService;

    @ApiOperation("委托查询列表")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<MatchOrderDetail> getUserDetail(
            @AuthForHeader @ApiParam(hidden = true) AuthContext context,
            @RequestParam(value = "uid", required = false) Integer uId,
            @RequestParam(value = "symbol", required = false) String symbol,
            @RequestParam(value = "outerOrderNo", required = false) String outerOrderNo,
            @RequestParam(value = "orderType", required = false) TradeCoinType orderType,
            @RequestParam(value = "status", required = false) TradeCoinStatus status,
            @RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
            @RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {

        Integer adminId = context.getLoginSession().getUserId();

        PageModel<MatchOrderDetail> pageModelDto = null;
        PageModel<TradeOrder> pageModel = agentConsignationTransService.queryConsignation(adminId, uId, symbol, outerOrderNo, orderType, status, startDate, endDate, pageNo, pageSize);
        pageModelDto = new PageModel<MatchOrderDetail>();
        if (null != pageModel) {

            List<MatchOrderDetail> lists = pageModel.getList().stream().map(a -> new MatchOrderDetail(a))
                    .collect(Collectors.toList());
            pageModelDto.setPageNo(pageModel.getPageNo());
            pageModelDto.setPageNum(pageModel.getPageNum());
            pageModelDto.setPageSize(pageModel.getPageSize());
            pageModelDto.setList(lists);

            int total = agentConsignationTransService.querytotal(adminId);

            pageModelDto.setTotal((long) total);

            int totaldealed = agentConsignationTransService.querytotaldealed(adminId);
            int totaldealing = agentConsignationTransService.querytotaldealing(adminId);

            pageModelDto.setTotaldealed(totaldealed);
            pageModelDto.setTotaldealing(totaldealing);
        }

        return pageModelDto;
    }
}
