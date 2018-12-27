package com.gte.configSector.controller;

import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.CurrencyType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.configSector.dto.ConfigSectorDto;
import com.gte.domain.sector.dto.ConfigSector;
import com.gte.domain.sector.enums.Sector;
import com.gte.sector.service.ConfigSectorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/configSectorController")
@Api(value="configSector",description="交易区")
public class ConfigSectorController {

    @Resource
    private ConfigSectorService configSectorService;


    @ApiOperation(value="查询所有交易区与交易币的关联关系")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public PageModel<ConfigSector> getConfigSector(@AuthForHeader AuthContext context,
                                                   @RequestParam(value = "sector",required = false) Sector sector,
                                                   @RequestParam(value = "assetCode",required = false) String assetCode,
                                                   @RequestParam(value = "pageNo",required = false) @ApiParam(defaultValue = "1") Integer pageNo,
                                                   @RequestParam(value = "pageSize",required = false) @ApiParam(defaultValue = "10") Integer pageSize) {
        ConfigSector dao = ConfigSector.builder()
                .assetCode(assetCode)
                .sector(sector).build();
        PageModel<ConfigSector> list = configSectorService.selectByPrimaryKeySelective(dao, pageNo, pageSize);
        return list;
    }

    @ApiOperation(value="查询一个交易区与交易币的关联关系")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/selectByPrimaryKey", method = RequestMethod.GET)
    @ResponseBody
    public ConfigSector selectByPrimaryKey(@AuthForHeader AuthContext context,@RequestParam(value = "id",required = false) Integer id) {
        return configSectorService.selectByPrimaryKey(id);
    }

    @ApiOperation(value="查询交易区（主板、创新板）")
    @RequestMapping(value = "/selectSector", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Integer> selectByPrimaryKey() {
        Map<String,Integer> mapSector =  new HashMap<>();
        mapSector.put(Sector.PRIMARY.name(), Sector.PRIMARY.ordinal());
        mapSector.put(Sector.INNOVATE.name(), Sector.INNOVATE.ordinal());
        return mapSector;
    }


    // 已有币种列表查询
    @ApiOperation(value="根据交易区（主板、创新板）查询币种列表")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/configassetListByCondition", method = RequestMethod.GET)
    public List<ConfigAsset> getConfigAssetList(@AuthForHeader AuthContext context,
                                                @RequestParam(value = "sector",required = false) Sector sector,
                                                @RequestParam(value = "assetCode",required = false) String assetCode,
                                                @RequestParam(value = "currencyType",required = false) CurrencyType currencyType) {
        ConfigSector record = ConfigSector.builder()
                .assetCode(assetCode)
                .sector(sector).build();
        List<ConfigAsset> list = configSectorService.getAssetCodeBySector(sector,assetCode,currencyType);
        return list;
    }

    // 已有交易对列表查询

    @ApiOperation(value="根据交易区（主板、创新板）查询交易对列表")
    @RequestMapping(value = "/getConfigSymbolListByCondition", method = RequestMethod.GET)
    public List<ConfigSymbol> getConfigSymbolListByCondition(@AuthForHeader AuthContext context,
                                                             @RequestParam(value = "sector",required = false) Sector sector,
                                                             @RequestParam(value = "assetCode",required = false) String assetCode,
                                                             @RequestParam(value = "currencyType",required = false) CurrencyType currencyType) {
        List<ConfigSymbol> list = configSectorService.getConfigSymbolListByCondition(sector,assetCode,currencyType);
        list.stream().forEach(c -> c.setTitle(c.getAssetCode2() + "/" + c.getAssetCode1()));
        return list;
    }
}
