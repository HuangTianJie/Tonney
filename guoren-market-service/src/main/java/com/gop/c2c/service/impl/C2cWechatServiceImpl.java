package com.gop.c2c.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gop.c2c.service.C2cWechatService;
import com.gop.code.consts.C2cCodeConst;
import com.gop.domain.C2cWeChatInfo;
import com.gop.exception.AppException;
import com.gop.mapper.C2cWechatMapper;
/**
 * 
 * @author zhangliwei
 *
 */
@Service("C2cWechatService")
public class C2cWechatServiceImpl implements C2cWechatService{
	@Autowired
	private C2cWechatMapper c2cWechatMapper;


	@Override
	@Transactional
	public void addC2cWechat(C2cWeChatInfo c2cWeChatInfo) {
		C2cWeChatInfo info  = c2cWechatMapper.selectStatusUsingByUid(c2cWeChatInfo.getUid());

		if (info !=  null) {
			int count = c2cWechatMapper.deleteById(info.getId());
			if(count == 1) {
				c2cWeChatInfo.setAddIndex(info.getAddIndex()+1);
				c2cWechatMapper.insertSelective(c2cWeChatInfo);
			}else {
				throw new AppException(C2cCodeConst.CREATE_ALIPAY_ERROR);
			}
		}else {
			c2cWeChatInfo.setAddIndex(1);
			c2cWechatMapper.insertSelective(c2cWeChatInfo);
		}		
	}

	public C2cWeChatInfo selectStatusUsingByUid(Integer uid) {
		C2cWeChatInfo info  = c2cWechatMapper.selectStatusUsingByUid(uid);
		return info;
	}

}
