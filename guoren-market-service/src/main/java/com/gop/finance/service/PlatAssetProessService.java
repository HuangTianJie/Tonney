package com.gop.finance.service;

import com.github.pagehelper.PageInfo;
import com.gop.domain.PlatAssetProcess;

import java.util.Date;
import java.util.List;

/**
 * Created by wuyanjie on 2018/5/28.
 */
public interface PlatAssetProessService {
    void insertBatch(List<PlatAssetProcess> platAssetProcessList);
    int selectCount(Date date);
    void updateStatus(Date date);
    PageInfo<PlatAssetProcess> selectPlatAssetProcess(Date endDate, String assetCode, Integer pageNo, Integer pageSize);
}
