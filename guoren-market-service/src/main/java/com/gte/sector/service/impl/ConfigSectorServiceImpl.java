package com.gte.sector.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.domain.ConfigAsset;
import com.gop.domain.ConfigAssetProfile;
import com.gop.domain.ConfigSymbol;
import com.gop.domain.enums.ConfigAssetType;
import com.gop.domain.enums.CurrencyType;
import com.gop.mode.vo.PageModel;
import com.gte.domain.sector.dto.ConfigSector;
import com.gte.domain.sector.enums.Sector;
import com.gte.mapper.sector.ConfigSectorMapper;
import com.gte.sector.service.ConfigSectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ConfigSectorServiceImpl implements ConfigSectorService {

    @Resource
    private ConfigSectorMapper configSectorMapper;

    @Transactional
    @Override
    public int deleteByPrimaryKey(Integer id) {

        if (configSectorMapper.selectByPrimaryKey(id) != null) {
            return configSectorMapper.deleteByPrimaryKey(id);
        }
        return -1;
    }

    @Transactional
    @Override
    public int insert(ConfigSector record) {
        if (configSectorMapper.selectByPrimaryKeySelective(ConfigSector.builder().assetCode(record.getAssetCode()).build()).size() == 0) {
            return configSectorMapper.insert(record);
        } else {
            return -1;
        }
    }

    @Transactional
    @Override
    public int insertSelective(ConfigSector record) {
        return configSectorMapper.insertSelective(record);
    }

    @Override
    public ConfigSector selectByPrimaryKey(Integer id) {
        return configSectorMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageModel<ConfigSector> selectByPrimaryKeySelective(ConfigSector record, Integer pageNo, Integer pageSize) {
        if (pageSize != null && pageSize != 0)
            PageHelper.startPage(pageNo, pageSize);
        PageModel<ConfigSector> pageModel = new PageModel<>();
        PageInfo<ConfigSector> pageInfo = new PageInfo<>(configSectorMapper.selectByPrimaryKeySelective(record));
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        pageModel.setPageNum(pageInfo.getPages());
        pageModel.setTotal(pageInfo.getTotal());
        pageModel.setList(pageInfo.getList());
        return pageModel;
    }

    @Transactional
    @Override
    public int updateByPrimaryKeySelective(ConfigSector record) {
        return configSectorMapper.updateByPrimaryKeySelective(record);
    }

    @Transactional
    @Override
    public int updateByAssetCodeSelective(ConfigSector record) {
        return configSectorMapper.updateByAssetCodeSelective(record);
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(ConfigSector record) {
        return configSectorMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ConfigAsset> getAssetCodeBySector(Sector sector, String assetCode, CurrencyType currencyType) {
        return configSectorMapper.getAssetCodeBySector(sector,assetCode,currencyType);
    }

    @Override
    public List<ConfigSymbol> getConfigSymbolListByCondition(Sector sector, String assetCode, CurrencyType currencyType) {
        return configSectorMapper.getConfigSymbolListByCondition(sector,assetCode,currencyType);
    }

    @Override
    public List<ConfigAssetProfile> getConfigAssetProfile(ConfigAssetType configAssetType,Sector sector,String assetCode,CurrencyType currencyType) {

        return configSectorMapper.getConfigAssetProfile(configAssetType,sector,assetCode,currencyType);
    }
}
