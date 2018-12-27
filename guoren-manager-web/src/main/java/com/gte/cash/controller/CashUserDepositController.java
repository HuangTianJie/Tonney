package com.gte.cash.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.CommonCodeConst;
import com.gop.common.ExportExcelService;
import com.gop.config.MailConfig;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.cash.dto.CashOrderDepositAuditDto;
import com.gte.cash.dto.CashOrderDepositUserDto;
import com.gte.cash.service.CashOrderDepositAuditService;
import com.gte.cash.service.CashOrderDepositUserService;
import com.gte.domain.cash.dto.CashOrderDepositAudit;
import com.gte.domain.cash.dto.CashOrderDepositUser;
import com.gte.domain.cash.enums.DepositResultStatus;
import com.gte.domain.cash.enums.DepositStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cash/recharge")
@Api(value = "recharge", description = "法币-管理用户充值")
public class CashUserDepositController {

    @Resource
    private CashOrderDepositUserService service;

    @Resource
    private CashOrderDepositAuditService auditService;
    @Resource
    private UserAccountFacade userAccountFacade;
    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private ExportExcelService exportExcelService;

    private static final String RECHARGE_INOUT_NAME = "rechargeInOut.xls";
    //private static final String RECHARGE_INOUT_NAME = "rechargeInOut"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMddHHmmss") + ".xls";
    private static final String RECHARGE_FTL ="recharge.ftl";

    @ApiOperation(value="查询用户资产的订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<CashOrderDepositUserDto> getList(@AuthForHeader AuthContext context,
                                                   @RequestParam(value = "assetCode",required = false) String assetCode,
                                                   @RequestParam(value = "orderNo",required = false) String orderNo,
                                                   @RequestParam(value = "status",required = false) DepositStatus status,
                                                   @RequestParam(value = "time",required = false)
                                                                  @ApiParam(required = false)
                                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date time,
                                                   @RequestParam(value = "pageNo",required = false) @ApiParam(defaultValue = "1") Integer pageNo,
                                                   @RequestParam(value = "pageSize",required = false) @ApiParam(defaultValue = "10") Integer pageSize) {
        CashOrderDepositUser dao = CashOrderDepositUser.builder()
                .assetCode(StringUtils.trimToNull(assetCode))
                .orderNo(StringUtils.trimToNull(orderNo))
                .status(status)
                .time(time)
                .build();
        PageModel<CashOrderDepositUser> result = service.selectBySelective(dao, pageNo, pageSize);
        PageModel<CashOrderDepositUserDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(result.getPageNum());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(result.getTotal());
        pageModel.setList(result.getList().stream().map(order -> new CashOrderDepositUserDto(order)).collect(Collectors.toList()));
        return pageModel;
    }

    @ApiOperation(value="查询一个订单&审核记录", notes = "查询一个订单 并显示订单审核纪录")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public CashOrderDepositUserDto get(@AuthForHeader AuthContext context, @RequestParam(value = "orderNo")String orderNo) {
        CashOrderDepositUserDto dto = new CashOrderDepositUserDto(service.selectByOrderNo(orderNo));
        dto.setFullName(userFacade.getUser(dto.getUid()).getFullname());
        List<CashOrderDepositAuditDto> auditDtoList = auditService.selectByOrderNo(orderNo).stream().map(audit-> new CashOrderDepositAuditDto(audit)).collect(Collectors.toList());
        dto.setAuditDtoList(auditDtoList);
        return dto;
    }


    @ApiOperation(value = "充值审核", notes = "审核一个订单")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping(value = "/approval")
    public int commitOrder(@AuthForHeader AuthContext context, @Valid @RequestBody CashOrderDepositAuditDto recordDto) {
        CashOrderDepositUser order = service.selectByOrderNo(recordDto.getOrderNo());
        if(order == null){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"没有订单！");
        }
        if(recordDto.getVersion() != order.getVersion()){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"订单版本号不一致！");
        }
        CashOrderDepositAudit.CashOrderDepositAuditBuilder build = CashOrderDepositAudit.builder().adminId(context.getLoginSession().getUserId())
                .beforeStatus(recordDto.getBeforeStatus())
                .afterStatus(recordDto.getAfterStatus())
                .result(recordDto.getResult())
                .orderNo(recordDto.getOrderNo())
                .explain(recordDto.getExplain());

        if(recordDto.getBeforeStatus().equals(DepositStatus.WAITAUDIT) && recordDto.getAfterStatus().equals(DepositStatus.CHECK)){
            build.result(DepositResultStatus.CONFIRM);
        }else if(recordDto.getBeforeStatus().equals(DepositStatus.CHECK) && recordDto.getAfterStatus().equals(DepositStatus.SUCCESS)){
            build.result(DepositResultStatus.SUCCESS);
        }else if(recordDto.getBeforeStatus().equals(DepositStatus.CHECK) && recordDto.getAfterStatus().equals(DepositStatus.WAITAUDIT)){
            build.result(DepositResultStatus.REFUSAL);
            if(StringUtils.isBlank(recordDto.getExplain())){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"驳回原因不能为空！");
            }
        }else{
            throw new AppException(CommonCodeConst.FIELD_ERROR,"非法操作！");
        }
        if(recordDto.getBeforeStatus().equals(DepositStatus.CHECK) && recordDto.getAfterStatus().equals(DepositStatus.WAITAUDIT)){
            order.setWatchful(1);
        }else if(recordDto.getAfterStatus().equals(DepositStatus.CHECK)){
            order.setWatchful(0);
        }else{
            order.setWatchful(null);
        }
        CashOrderDepositAudit record = build.build();
        if(recordDto.getBeforeStatus().equals(DepositStatus.WAITAUDIT) && recordDto.getAfterStatus().equals(DepositStatus.CHECK)){
            if(recordDto.getRealNumber() == null || recordDto.getRealNumber().equals(BigDecimal.ZERO)){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"到账金额不能为空、零！");
            }
            if(recordDto.getRealNumber().compareTo(order.getNumber()) == 1){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"到账金额不能大于汇款金额！");
            }
        }else{
            recordDto.setRealNumber(BigDecimal.ZERO);
        }
        if(!order.getStatus().equals(record.getBeforeStatus())){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"订单状态已变化！");
        }
        return service.approval(record,order,recordDto.getRealNumber());
    }

    @Strategys(strategys = {//@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ApiOperation("导出订单")
    public String export(@AuthForHeader AuthContext context,
                                  @RequestParam(value = "assetCode",required = false) String assetCode,
                                  @RequestParam(value = "orderNo",required = false) String orderNo,
                                  @RequestParam(value = "status",required = false) DepositStatus status,
                                  @RequestParam(value = "time",required = false)
                                      @ApiParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") Date time) {
        CashOrderDepositUser dao = CashOrderDepositUser.builder()
                .assetCode(StringUtils.trimToNull(assetCode))
                .orderNo(StringUtils.trimToNull(orderNo))
                .status(status)
                .time(time)
                .build();
        PageModel<CashOrderDepositUser> result = service.selectBySelective(dao,null,null);
        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(CashOrderDepositUser order : result.getList()){
            JSONObject o = (JSONObject) JSON.toJSON(order);
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("orderNo",order.getOrderNo());
            resMap.put("uid",order.getUid());
            resMap.put("fullName",userFacade.getUser(order.getUid()).getFullname());
            resMap.put("codeNo",order.getCodeNo());
            resMap.put("assetCode",order.getAssetCode());
            resMap.put("cardName",order.getCardName());
            resMap.put("cardNumber",order.getCardNumber());
            resMap.put("number",order.getNumber());
            resMap.put("time",o.getString("time"));
            resMap.put("createDate",o.getString("createDate"));
            resMap.put("status",order.getStatus().getDesc());
            resMap.put("realNumber",order.getRealNumber());
            resMap.put("adminId",toString(order.getAdminId()));
            resMap.put("watchful",order.getWatchful());
            resMap.put("updateDate",o.getString("updateDate"));
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName(RECHARGE_FTL,resultMerStlMap, mailConfig.getReportRoot()+RECHARGE_INOUT_NAME);
        return mailConfig.getReportDownload()+RECHARGE_INOUT_NAME;
    }

    private String toString(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

}
