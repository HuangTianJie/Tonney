package com.gop.mapper;

import com.gop.domain.C2cWeChatInfo;

public interface C2cWechatMapper {
    int insertSelective(C2cWeChatInfo record);

    int deleteById(Integer id);
    
    C2cWeChatInfo selectStatusUsingByUid(Integer uid);
    
}