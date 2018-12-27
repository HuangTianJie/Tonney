package com.gte.agent.service;


import com.github.pagehelper.PageInfo;
import com.gte.domain.agent.Agent;

public interface AgentService {
	Agent getAgentByAccount(String account);
	Agent getAgentByByAdminId(Integer adminId);

    PageInfo<Agent> getAgentList(Integer pageNo, Integer pageSize);
	int insert (Agent agent);

	int update(Agent agent);
}
