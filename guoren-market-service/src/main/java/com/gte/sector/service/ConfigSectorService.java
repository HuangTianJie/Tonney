package com.gte.sector.service;

import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gop.mode.vo.PageModel;
import com.gte.domain.sector.dto.ConfigSector;
import com.gte.domain.sector.enums.Sector;

import java.util.List;

public interface ConfigSectorService {

    int deleteByPrimaryKey(Integer id);

    int insert(ConfigSector record);

    int insertSelective(ConfigSector record);

    ConfigSector selectByPrimaryKey(Integer id);

    PageModel<ConfigSector> selectByPrimaryKeySelective(ConfigSector record,Integer pageNo,Integer pageSize);

    int updateByPrimaryKeySelective(ConfigSector record);

    int updateByAssetCodeSelective(ConfigSector record);

    int updateByPrimaryKey(ConfigSector record);

    /**
     * 根据交易区（主板、创新板）查询交易币种列表
     * @param sector 交易区
     * @param assetCode 交易币/资产代码
     * @param currencyType 资产种类
     * @return
     */
    List<ConfigAsset> getAssetCodeBySector(Sector sector, String assetCode, CurrencyType currencyType);

    List<ConfigSymbol> getConfigSymbolListByCondition(Sector sector, String assetCode, CurrencyType currencyType);

    List<ConfigAssetProfile> getConfigAssetProfile(ConfigAssetType key, Sector sector, String assetCode, CurrencyType currencyType);
}
