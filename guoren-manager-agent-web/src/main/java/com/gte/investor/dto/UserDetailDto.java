package com.gte.investor.dto;

import com.gop.domain.UserBasicInfo;
import com.gop.domain.UserIdentification;
import com.gop.domain.UserResidence;
import lombok.Data;

import java.util.List;

@Data
public class UserDetailDto {
	
	private UserInfoDto userInfoDto;
	private UserBasicInfo userBasicInfo;
	private UserIdentification userIdentification;
	private UserResidence userResidence;
	private List<UserIdentification> userIdentifications;
	private List<UserResidence> userResidences;

}
