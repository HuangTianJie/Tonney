package com.gte.cash.controller;

import com.gop.Swagger2;
import com.gop.asset.dto.UserAccountDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.DebitCardConfigConst;
import com.gop.code.consts.UserAssetCodeConst;
import com.gop.conetxt.WebApiResponseFactory;
import com.gop.domain.ConfigAsset;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.util.OrderUtil;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.cash.dto.CashOrderDepositUserDto;
import com.gte.cash.dto.DebitCardConfigDto;
import com.gte.cash.service.CashOrderDepositUserService;
import com.gte.cash.service.CashUserCardService;
import com.gte.domain.cash.dto.CashOrderDepositUser;
import com.gte.domain.cash.dto.CashUserCard;
import com.gte.domain.cash.enums.DepositStatus;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cash/recharge")
@Api(value = "recharge", description = "法币-用户充值")
public class CashUserDepositController {
    private static final String pattern="000000";
    @Resource
    private CashOrderDepositUserService service;
    @Resource
    private UserAccountFacade userAccountFacade;

    @Resource
    private CashUserCardService cashUserCardService;

    @Resource
    private ConfigAssetService configAssetService;

    @Autowired
    @Qualifier("webApiResponseFactory")
    WebApiResponseFactory webApiResponseFactory;

    @ApiOperation(value="查询用户资产的订单")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/listByAssetCode", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<CashOrderDepositUserDto> getConfigSector(@AuthForHeader AuthContext context,
                                                   @RequestParam(value = "assetCode",required = false) String assetCode,
                                                   @RequestParam(value = "pageNo",required = false) @ApiParam(defaultValue = "1") Integer pageNo,
                                                   @RequestParam(value = "pageSize",required = false) @ApiParam(defaultValue = "10") Integer pageSize) {
        CashOrderDepositUser dao = CashOrderDepositUser.builder().assetCode(StringUtils.trimToNull(assetCode)).uid(context.getLoginSession().getUserId()).build();
        PageModel<CashOrderDepositUser> result = service.selectBySelective(dao, pageNo, pageSize);
        PageModel<CashOrderDepositUserDto> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(result.getPageNum());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(result.getTotal());
        pageModel.setList(result.getList().stream().map(order -> new CashOrderDepositUserDto(order)).collect(Collectors.toList()));
        return pageModel;
    }

    @ApiOperation(value = "用户充值提交")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="header", name = "authorization", value = "令牌", required = true, dataType = "String",defaultValue="pay-password=a123456789,token="+ Swagger2.token  )
    })
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
    @PostMapping(value = "/commitOrder")
    public String commitOrder(@AuthForHeader AuthContext context, @Valid @RequestBody CashOrderDepositUserDto recordDto) {
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
        CashOrderDepositUser record = CashOrderDepositUser.builder().uid(context.getLoginSession().getUserId())
                .account(userAccountDto.getAccountNo())
                .accountId(userAccountDto.getAccountId())
                .assetCode(assetCode)
                .orderNo(orderNo)
                .number(recordDto.getNumber())
                .cardName(recordDto.getCardName())
                .cardNumber(recordDto.getCardNumber())
                .codeNo(recordDto.getCodeNo())
                .fileName(recordDto.getFileName())
                .fee(recordDto.getFee())
                .brokerId(recordDto.getBrokerId())
                .status(DepositStatus.WAITAUDIT)
                .time(recordDto.getTime())
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
    public CashOrderDepositUserDto get(@AuthForHeader AuthContext context, @RequestParam("orderNo") String orderNo) {
        CashOrderDepositUserDto dto = new CashOrderDepositUserDto(service.selectByOrderNo(orderNo));
        return dto;
    }

    @ApiOperation(value="查询用户的识别码", notes = "查询用户的识别码-- 已废弃  请使用 查询收款信息(/getDebitCardConfig)")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/getIdentificationCode", method = RequestMethod.GET)
    @ResponseBody
    public String get(@AuthForHeader AuthContext context) {
        //33 是杂质
        String uid = Long.toString(context.getLoginSession().getUserId() + 33, 36).toUpperCase();
        return pattern.substring(0, pattern.length()-uid.length())+uid;
    }

    @ApiOperation(value="查询收款信息", notes = "查询收款信息")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/getDebitCardConfig", method = RequestMethod.GET)
    @ResponseBody
    public DebitCardConfigDto getDebitCardConfig(@AuthForHeader AuthContext context) {
        String uid = Long.toString(context.getLoginSession().getUserId() + 33, 36).toUpperCase();
        String[] code = {pattern.substring(0, pattern.length()-uid.length())+uid};
        Locale locale = LocaleContextHolder.getLocale();
        DebitCardConfigDto.DebitCardConfigDtoBuilder builder = DebitCardConfigDto.builder();
        builder.remittanceWay(webApiResponseFactory.get(DebitCardConfigConst.DEBIT_CARD_REMITTANCE_WAY, null, null, locale).getMsg());
        builder.receiveBank(webApiResponseFactory.get(DebitCardConfigConst.DEBIT_CARD_RECEIVE_BANK,null,null,locale).getMsg());
        builder.receiveAccount(webApiResponseFactory.get(DebitCardConfigConst.DEBIT_CARD_RECEIVE_ACCOUNT, null, null, locale).getMsg());
        builder.receiveNumber(webApiResponseFactory.get(DebitCardConfigConst.DEBIT_CARD_RECEIVE_NUMBER, null, null, locale).getMsg());
        builder.receiveRemark(webApiResponseFactory.get(DebitCardConfigConst.DEBIT_CARD_RECEIVE_REMARK, code, null, locale).getMsg());
        return builder.build();
    }
}
