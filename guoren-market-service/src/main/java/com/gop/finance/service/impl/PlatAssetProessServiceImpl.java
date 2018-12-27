package com.gop.finance.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.domain.PlatAssetProcess;
import com.gop.finance.service.PlatAssetProessService;
import com.gop.mapper.PlatAssetProessMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by wuyanjie on 2018/5/28.
 */
@Service
@Slf4j
public class PlatAssetProessServiceImpl implements PlatAssetProessService{
    @Autowired
    private PlatAssetProessMapper platAssetProessMapper;
    @Override
    public void insertBatch(List<PlatAssetProcess> platAssetProcessList) {
        platAssetProessMapper.insertBatch(platAssetProcessList);
    }

    @Override
    public int selectCount(Date date) {
        return platAssetProessMapper.selectCount(date);
    }

    @Override
    public void updateStatus(Date date) {
        platAssetProessMapper.updateStatus(date);
    }

    @Override
    public PageInfo<PlatAssetProcess> selectPlatAssetProcess(Date endDate, String assetCode, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        return new PageInfo<>(platAssetProessMapper.selectList(assetCode,endDate));
    }
}
