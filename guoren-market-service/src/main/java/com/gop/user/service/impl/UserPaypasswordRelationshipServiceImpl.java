package com.gop.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.domain.UserPaypasswordRelationship;
import com.gop.mapper.UserPaypasswordRelationshipMapper;
import com.gop.user.service.UserPaypasswordRelatationService;

@Service("UserPaypasswordRelatationService")
public class UserPaypasswordRelationshipServiceImpl implements UserPaypasswordRelatationService {
	@Autowired
	private UserPaypasswordRelationshipMapper userPaypasswordRelationshipMapper;

	@Override
	public UserPaypasswordRelationship selectByUid(Integer uid) {
		return userPaypasswordRelationshipMapper.selectByUid(uid);
	}

	@Override
	public void updateByUid(UserPaypasswordRelationship userPaypasswordRelationship) {
		userPaypasswordRelationshipMapper.updateByUid(userPaypasswordRelationship);
	}
	
	@Override
	public void insertInfo(UserPaypasswordRelationship userPaypasswordRelationship) {
		userPaypasswordRelationshipMapper.insertInfo(userPaypasswordRelationship);
	}
}
