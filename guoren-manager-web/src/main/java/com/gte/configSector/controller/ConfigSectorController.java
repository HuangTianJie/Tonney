package com.gte.configSector.controller;

import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gop.exception.AppException;
import com.gop.match.dto.WithdrawCoinFeeConfigDto;
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
import java.util.*;

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
    @ApiOperation(value="删除交易区与交易币的关联关系")
    @Strategys(strategys = {// @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/deleteByPrimaryKey", method = RequestMethod.GET)
    @ResponseBody
    public int deleteByPrimaryKey(@AuthForHeader AuthContext context,@RequestParam("id") Integer id) {
        int reslut = configSectorService.deleteByPrimaryKey(id);
        if(reslut == -1){
            throw new AppException(CommonCodeConst.FIELD_ERROR, "删除失败,交易币不存在!");
        }
        return reslut;
    }

    @ApiOperation(value="新增交易区与交易币的关联关系")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ResponseBody
    public int insert(@AuthForHeader AuthContext context,@RequestBody ConfigSectorDto dto) {
        ConfigSector record = ConfigSector.builder()
                .id(dto.getId())
                .assetCode(dto.getAssetCode())
                .sector(dto.getSector()).build();
        int reslut = 0;
        try {
            reslut =  configSectorService.insert(record);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new AppException(CommonCodeConst.FIELD_ERROR, "新增失败!");
        }
        if(reslut == -1){
            throw new AppException(CommonCodeConst.FIELD_ERROR, "新增失败,交易币已存在!");
        }
        return reslut;
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
//    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
//            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
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

    // 转账配置查询
    @ApiOperation(value="根据交易区（主板、创新板）查询资产配置参数")
    @Strategys(strategys = { //@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @RequestMapping(value = "/getConfigAssetProfile-list", method = RequestMethod.GET)
    public List<WithdrawCoinFeeConfigDto> withdrawCoinFeeConfigList(@AuthForHeader AuthContext context,
                                                                    @RequestParam("key") ConfigAssetType key,
                                                                    @RequestParam(value = "assetCode", required = false) String assetCode,
                                                                    @RequestParam(value = "sector",required = false) Sector sector,
                                                                    @RequestParam(value = "currencyType",required = false) CurrencyType currencyType) {

        // 设置返回list
        List<WithdrawCoinFeeConfigDto> dtoList = new ArrayList<>();

        // 查询数据库中的对应list
        List<ConfigAssetProfile> list = configSectorService.getConfigAssetProfile(key,sector,assetCode,currencyType);

        // 转换查询list为dtolist
        if (null != list) {
            for (ConfigAssetProfile configAssetProfile : list) {
                WithdrawCoinFeeConfigDto dto = new WithdrawCoinFeeConfigDto();
                dto.setId(configAssetProfile.getId());
                dto.setAssetCode(configAssetProfile.getAssetCode());
                dto.setProfileKey(configAssetProfile.getProfileKey());
                dto.setProfileValue(configAssetProfile.getProfileValue());
                dtoList.add(dto);
            }
        } else {
            dtoList = Collections.EMPTY_LIST;
        }
        return dtoList;
    }
}
