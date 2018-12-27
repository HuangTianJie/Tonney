package com.gop.c2c.service;

import com.gop.domain.C2cWeChatInfo;

/**
 * 
 * @author zhangliwei
 *
 */
public interface C2cWechatService {

	public void addC2cWechat(C2cWeChatInfo c2cWeChatInfo);
	
	C2cWeChatInfo selectStatusUsingByUid(Integer uid);
}
