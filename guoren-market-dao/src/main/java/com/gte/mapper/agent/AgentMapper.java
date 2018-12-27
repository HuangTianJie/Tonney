package com.gte.mapper.agent;

import com.gte.domain.agent.Agent;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface AgentMapper {
	
	int deleteByPrimaryKey(Integer adminId);

	int insertSelective(Agent record);

	Agent selectByPrimaryKey(Integer adminId);
	
	int updateByPrimaryKeySelective(Agent record);

	int updateByPrimaryKey(Agent record);
	
	Agent selectByAccount(@Param("mobile")String phone);

	List<Agent> getAgentList();
}