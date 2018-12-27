package com.gte.cash.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.gte.cash.dto.CashOrderWithdrawAuditDto;
import com.gte.cash.dto.CashOrderWithdrawUserDto;
import com.gte.cash.dto.CashOrderWithdrawUserFeeDto;
import com.gte.cash.service.CashOrderWithdrawAuditService;
import com.gte.cash.service.CashOrderWithdrawUserFeeService;
import com.gte.cash.service.CashOrderWithdrawUserService;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;
import com.gte.domain.cash.enums.WithdrawResultStatus;
import com.gte.domain.cash.enums.WithdrawStatus;
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
@RequestMapping("/cash/withdraw")
@Api(value = "withdraw", description = "法币-管理用户提现")
public class CashUserWithdrawController {

    @Resource
    private CashOrderWithdrawUserService service;

    @Resource
    private CashOrderWithdrawAuditService auditService;

    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private ExportExcelService exportExcelService;
    @Autowired
    private CashOrderWithdrawUserFeeService cashOrderWithdrawUserFeeService;

    private static final String RECHARGE_INOUT_NAME = "withdrawInOut.xls";
    //private static final String RECHARGE_INOUT_NAME = "rechargeInOut"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMddHHmmss") + ".xls";
    private static final String WITHDRAW_FTL ="withdraw.ftl";

    @ApiOperation(value="查询用户资产的订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<CashOrderWithdrawUserDto> getList(@AuthForHeader AuthContext context,
                                                      @RequestParam(value = "assetCode",required = false) String assetCode,
                                                      @RequestParam(value = "orderNo",required = false) String orderNo,
                                                      @RequestParam(value = "status",required = false) WithdrawStatus status,
                                                      @RequestParam(value = "time",required = false)
                                                      @ApiParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") Date time,
                                                      @RequestParam(value = "pageNo",required = false) @ApiParam(defaultValue = "1") Integer pageNo,
                                                      @RequestParam(value = "pageSize",required = false) @ApiParam(defaultValue = "10") Integer pageSize) {
        CashOrderWithdrawUser dao = CashOrderWithdrawUser.builder()
                .assetCode(StringUtils.trimToNull(assetCode))
                .orderNo(StringUtils.trimToNull(orderNo))
                .status(status)
                .createDate(time)
                .build();
        PageModel<CashOrderWithdrawUser> result = service.selectBySelective(dao, pageNo, pageSize);
        PageModel<CashOrderWithdrawUserDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(result.getPageNum());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(result.getTotal());
        pageModel.setList(result.getList().stream().map(order -> new CashOrderWithdrawUserDto(order)).collect(Collectors.toList()));
        return pageModel;
    }

    @ApiOperation(value="查询一个订单", notes = "查询一个订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public CashOrderWithdrawUserDto get(@AuthForHeader AuthContext context, @RequestParam("orderNo") String orderNo) {
        CashOrderWithdrawUser order = service.selectByOrderNo(orderNo);
        if(order == null){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"没有订单！");
        }
        CashOrderWithdrawUserDto dto = new CashOrderWithdrawUserDto(order);
        dto.setFullName(userFacade.getUser(dto.getUid()).getFullname());
        List<CashOrderWithdrawAudit> auditList = auditService.selectByOrderNo(orderNo);
        dto.setAuditDtoList(auditList);
        List<CashOrderWithdrawUserFee>  feeList = cashOrderWithdrawUserFeeService.selectByOrderNo(orderNo);
        dto.setFeeList(feeList.stream().map(o->new CashOrderWithdrawUserFeeDto(o)).collect(Collectors.toList()));
        return dto;
    }

    @ApiOperation(value = "提现审核", notes = "审核一个订单")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping(value = "/approval")
    public int commitOrder(@AuthForHeader AuthContext context, @Valid @RequestBody CashOrderWithdrawAuditDto recordDto) {
        CashOrderWithdrawUser order = service.selectByOrderNo(recordDto.getOrderNo());
        if(order == null){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"没有订单！");
        }
        if(recordDto.getVersion() != order.getVersion()){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"订单版本号不一致！");
        }
        CashOrderWithdrawAudit.CashOrderWithdrawAuditBuilder build = CashOrderWithdrawAudit.builder().adminId(context.getLoginSession().getUserId())
                .beforeStatus(recordDto.getBeforeStatus())
                .afterStatus(recordDto.getAfterStatus())
                .result(recordDto.getResult())
                .orderNo(recordDto.getOrderNo())
                .explain(recordDto.getExplain());
        if(recordDto.getBeforeStatus().equals(WithdrawStatus.WAITAUDIT) && recordDto.getAfterStatus().equals(WithdrawStatus.CONFIRM)){
            build.result(WithdrawResultStatus.AUTHORIZE);
        }else if(recordDto.getBeforeStatus().equals(WithdrawStatus.CONFIRM) && recordDto.getAfterStatus().equals(WithdrawStatus.DEDUCT)){
            build.result(WithdrawResultStatus.CONFIRM);
        }else if(recordDto.getBeforeStatus().equals(WithdrawStatus.CONFIRM) && recordDto.getAfterStatus().equals(WithdrawStatus.REFUSE)){
            if(StringUtils.isBlank(recordDto.getExplain())){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"失败原因不能为空！");
            }
            build.result(WithdrawResultStatus.REFUSE);
        }else if(recordDto.getBeforeStatus().equals(WithdrawStatus.DEDUCT) && recordDto.getAfterStatus().equals(WithdrawStatus.SUCCESS)){
            build.result(WithdrawResultStatus.SUCCESS);
        }else if(recordDto.getBeforeStatus().equals(WithdrawStatus.DEDUCT) && recordDto.getAfterStatus().equals(WithdrawStatus.CONFIRM)){
            build.result(WithdrawResultStatus.REFUSAL);
            if(StringUtils.isBlank(recordDto.getExplain())){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"驳回原因不能为空！");
            }
        }else{
            throw new AppException(CommonCodeConst.FIELD_ERROR,"非法操作！");
        }
        if(recordDto.getBeforeStatus().equals(WithdrawStatus.DEDUCT) && recordDto.getAfterStatus().equals(WithdrawStatus.CONFIRM)){
            order.setWatchful(1);
        }else if(recordDto.getAfterStatus().equals(WithdrawStatus.DEDUCT)){
            order.setWatchful(0);
        }else{
            order.setWatchful(null);
        }
        CashOrderWithdrawAudit record = build.build();
                List<CashOrderWithdrawUserFee> list = null;

       if(recordDto.getBeforeStatus().equals(WithdrawStatus.CONFIRM) && recordDto.getAfterStatus().equals(WithdrawStatus.DEDUCT)){

            if(recordDto.getFeeList() == null || recordDto.getFeeList().size() == 0
                    || recordDto.getFeeList().stream().filter(dto -> dto.getTxFee()==null || dto.getTxFee().compareTo(BigDecimal.ZERO) <= 0).count()>0){
                throw new AppException(CommonCodeConst.FIELD_ERROR,"手续费不能含有空、零、负数！");
            }
           list = recordDto.getFeeList().stream().map(fee-> CashOrderWithdrawUserFee.builder().assetCode(fee.getAssetCode())
                   .createDate(fee.getCreateDate())
                   .orderNo(recordDto.getOrderNo())
                   .name(fee.getName())
                   .txFee(fee.getTxFee()).build()).collect(Collectors.toList());
        }
        if(!order.getStatus().equals(record.getBeforeStatus())){
            throw new AppException(CommonCodeConst.FIELD_ERROR,"订单状态已变化！");
        }
        return service.approval(record,order,list);
    }


    @Strategys(strategys = {//@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ApiOperation("导出订单")
    public String export(@AuthForHeader AuthContext context,
                         @RequestParam(value = "assetCode",required = false) String assetCode,
                         @RequestParam(value = "orderNo",required = false) String orderNo,
                         @RequestParam(value = "status",required = false) WithdrawStatus status,
                         @RequestParam(value = "time",required = false)
                             @ApiParam(required = false)
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date time) {
        CashOrderWithdrawUser dao = CashOrderWithdrawUser.builder()
                .assetCode(StringUtils.trimToNull(assetCode))
                .orderNo(StringUtils.trimToNull(orderNo))
                .status(status)
                .createDate(time)
                .build();
        PageModel<CashOrderWithdrawUser> result = service.selectBySelective(dao, null,null);

        Map<String, Object> resultMerStlMap = new HashMap<String, Object>();
        List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
        for(CashOrderWithdrawUser order : result.getList()){
            JSONObject o = (JSONObject) JSON.toJSON(order);
            Map<String, Object> resMap = new HashMap<String, Object>();
            resMap.put("orderNo",order.getOrderNo());
            resMap.put("uid",order.getUid());
            resMap.put("fullName",userFacade.getUser(order.getUid()).getFullname());
            resMap.put("assetCode",order.getAssetCode());
            resMap.put("cardName",order.getCardName());
            resMap.put("cardNumber",order.getCardNumber());
            resMap.put("number",order.getNumber());
            resMap.put("realNumber",order.getRealNumber());
            resMap.put("txFee",order.getTxFee());
            resMap.put("bankName",order.getBankName());
            resMap.put("swiftCode",order.getSwiftCode());
            resMap.put("createDate",o.getString("createDate"));
            resMap.put("status",order.getStatus().getDesc());
            resMap.put("adminId",toString(order.getAdminId()));
            resMap.put("watchful",order.getWatchful());
            resMap.put("updateDate",o.getString("updateDate"));
            lst.add(resMap);
        }
        resultMerStlMap.put("resultList", lst);
        exportExcelService.createTemplateXlsByFileName(WITHDRAW_FTL,resultMerStlMap, mailConfig.getReportRoot()+RECHARGE_INOUT_NAME);
        return mailConfig.getReportDownload()+RECHARGE_INOUT_NAME;
    }

    private String toString(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

}
