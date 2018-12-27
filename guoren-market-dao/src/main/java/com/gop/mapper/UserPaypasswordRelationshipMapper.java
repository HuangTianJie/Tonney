package com.gop.mapper;

import com.gop.domain.UserPaypasswordRelationship;

public interface UserPaypasswordRelationshipMapper {

	UserPaypasswordRelationship selectByUid(Integer uid);
	
	void updateByUid(UserPaypasswordRelationship userPaypasswordRelationship);
	
	void insertInfo(UserPaypasswordRelationship userPaypasswordRelationship);

}