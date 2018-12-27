package com.gop.user.service;

import com.gop.domain.UserPaypasswordRelationship;

public interface UserPaypasswordRelatationService {

	public UserPaypasswordRelationship selectByUid(Integer uid);

	public void updateByUid(UserPaypasswordRelationship userPaypasswordRelationship);
	
	public void insertInfo(UserPaypasswordRelationship userPaypasswordRelationship);
	
}
