package com.gte.cash.controller;

import com.gop.code.consts.CommonCodeConst;
import com.gop.exception.AppException;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.cash.dto.CashUserCardDto;
import com.gte.cash.service.CashUserCardService;
import com.gte.domain.cash.dto.CashUserCard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cash/card")
@Api(value = "cash", description = "法币-银行卡")
public class CashUserCardController {


    @Resource
    private  CashUserCardService CashUserCardService;

    @ApiOperation(value = "查询用户名下的银行卡")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @GetMapping(value = "/getAll")
    public List<CashUserCardDto> getAll(@AuthForHeader AuthContext context) {
        List<CashUserCard> list = CashUserCardService.selectSelective(CashUserCard.builder().uid(context.getLoginSession().getUserId()).build());
        return list.stream().map(card -> new CashUserCardDto(card)).collect(Collectors.toList());
    }

    @ApiOperation(value = "添加用户名下的银行卡")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping(value = "/add")
    public int add(@AuthForHeader AuthContext context,@Valid @RequestBody CashUserCardDto record) {
        int uid = context.getLoginSession().getUserId();
        List<CashUserCard> list = CashUserCardService.selectSelective(CashUserCard.builder().uid(uid).build());
        if (list.size() > 10) {
            throw new AppException(CommonCodeConst.FIELD_ERROR, "银行卡过多！");
        }
        CashUserCard card = CashUserCard.builder().name(record.getName()).cardNumber(record.getCardNumber()).uid(uid).build();
        return CashUserCardService.insertSelective(card);
    }

    @ApiOperation(value = "删除用户名下的银行卡")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping(value = "/del")
    public int del(@AuthForHeader AuthContext context, int id) {
        int uid = context.getLoginSession().getUserId();
        List<CashUserCard> list = CashUserCardService.selectSelective(CashUserCard.builder().uid(uid).id(id).build());
        if (list.size() == 0) {
            throw new AppException(CommonCodeConst.FIELD_ERROR, "没有查到对应的银行卡！");
        }
        return CashUserCardService.deleteByPrimaryKey(list.get(0).getId());
    }
}
