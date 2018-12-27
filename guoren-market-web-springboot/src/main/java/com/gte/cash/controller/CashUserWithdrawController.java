package com.gte.cash.controller;

import com.gop.Swagger2;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.util.OrderUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.cash.dto.CashOrderWithdrawUserDto;
import com.gte.cash.service.CashOrderWithdrawUserService;
import com.gte.cash.service.CashUserCardService;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.dto.CashUserCard;
import com.gte.domain.cash.enums.WithdrawStatus;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cash/withdraw")
@Api(value = "withdraw", description = "法币-用户提现")
public class CashUserWithdrawController {
    @Resource
    private CashOrderWithdrawUserService service;
    @Resource
    private ConfigAssetService configAssetService;
    @Resource
    private UserAccountFacade userAccountFacade;
    @Resource
    private CashUserCardService cashUserCardService;

    @ApiOperation(value="查询用户资产的订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/listByAssetCode", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<CashOrderWithdrawUserDto> getConfigSector(@AuthForHeader AuthContext context,
                                                              @RequestParam(value = "assetCode",required = false) String assetCode,
                                                              @RequestParam(value = "pageNo",required = false) @ApiParam(defaultValue = "1") Integer pageNo,
                                                              @RequestParam(value = "pageSize",required = false) @ApiParam(defaultValue = "10") Integer pageSize) {
        CashOrderWithdrawUser dao = CashOrderWithdrawUser.builder().assetCode(StringUtils.trimToNull(assetCode)).uid(context.getLoginSession().getUserId()).build();
        PageModel<CashOrderWithdrawUser> result = service.selectBySelective(dao, pageNo, pageSize);
        PageModel<CashOrderWithdrawUserDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(result.getPageNum());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(result.getTotal());
        pageModel.setList(result.getList().stream().map(order -> new CashOrderWithdrawUserDto(order)).collect(Collectors.toList()));
        return pageModel;
    }

    @ApiOperation(value = "用户提现提交")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="header", name = "authorization", value = "令牌", required = true, dataType = "String",defaultValue="pay-password=a123456789,token="+ Swagger2.token)
    })
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
    @PostMapping(value = "/commitOrder")
    public String commitOrder(@AuthForHeader AuthContext context, @Valid @RequestBody CashOrderWithdrawUserDto recordDto) {
        String assetCode = recordDto.getAssetCode();
        Integer uid = context.getLoginSession().getUserId();
        if (null != assetCode) {
            ConfigAsset configAsset = configAssetService.getAssetConfig(assetCode);
            if (configAsset == null) {
                throw new AppException(UserAssetCodeConst.INVALID_ASSET_CODE);
            }
        }
        UserAccountDto userAccountDto = userAccountFacade.queryAccount(uid, assetCode);
        String orderNo = OrderUtil.generateCode(OrderUtil.TRANSFER_SERVICE, OrderUtil.TRANSFER_IN_CURRENCY);
        CashOrderWithdrawUser record = CashOrderWithdrawUser.builder().uid(context.getLoginSession().getUserId())
                .account(userAccountDto.getAccountNo())
                .accountId(userAccountDto.getAccountId())
                .assetCode(assetCode)
                .orderNo(orderNo)
                .number(recordDto.getNumber())
                .realNumber(recordDto.getRealNumber())
                .txFee(recordDto.getTxFee())
                .cardName(recordDto.getCardName())
                .cardNumber(recordDto.getCardNumber())
                .bankName(recordDto.getBankName())
                .swiftCode(recordDto.getSwiftCode())
                .brokerId(recordDto.getBrokerId())
                .status(WithdrawStatus.WAITAUDIT)
                .build();
        int i = service.insertSelective(record);

        //记录使用过的银行卡
        if(i == 1){
            List<CashUserCard> list = cashUserCardService.selectSelective(CashUserCard.builder().uid(uid).build());
            if (list.size() < 3) {
                boolean isSave =true;
                for (CashUserCard c:  list) {
                    if(c.getCardNumber().equals(recordDto.getCardNumber())){
                        isSave = false;
                    }
                }
                if (isSave)cashUserCardService.insertSelective(CashUserCard.builder().name(recordDto.getCardName()).cardNumber(recordDto.getCardNumber()).uid(uid).build());
            }
        }
        return orderNo;
    }

    @ApiOperation(value="查询一个订单", notes = "查询一个订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public CashOrderWithdrawUserDto get(@AuthForHeader AuthContext context, @RequestParam("orderNo") String orderNo) {
        CashOrderWithdrawUserDto dto = new CashOrderWithdrawUserDto(service.selectByOrderNo(orderNo));
        return dto;
    }
}
