package com.gte.mapper.sector;

import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gte.domain.sector.dto.ConfigSector;
import com.gte.domain.sector.enums.Sector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigSectorMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ConfigSector record);

    int save(ConfigSector record);

    int insertSelective(ConfigSector record);

    ConfigSector selectByPrimaryKey(Integer id);

    List<ConfigSector> selectByPrimaryKeySelective(ConfigSector record);

    int updateByPrimaryKeySelective(ConfigSector record);

    int updateByAssetCodeSelective(ConfigSector record);

    int updateByPrimaryKey(ConfigSector record);

    List<ConfigAsset> getAssetCodeBySector(@Param("sector")Sector sector, @Param("assetCode")String assetCode,@Param("currencyType") CurrencyType currencyType);

    List<ConfigSymbol> getConfigSymbolListByCondition(@Param("sector")Sector sector, @Param("assetCode")String assetCode,@Param("currencyType") CurrencyType currencyType);

    List<ConfigAssetProfile> getConfigAssetProfile(@Param("profileKey") ConfigAssetType configAssetType, @Param("sector")Sector sector, @Param("assetCode")String assetCode, @Param("currencyType") CurrencyType currencyType);
}