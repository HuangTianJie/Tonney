package com.gte.agent.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.Administrators;
import com.gop.domain.enums.LockStatus;
import com.gop.exception.AppException;
import com.gop.user.service.AdministractorService;
import com.gop.user.service.RoleManagerService;
import com.gte.agent.service.AgentService;
import com.gte.domain.agent.Agent;
import com.gte.mapper.agent.AgentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class AgentServiceImpl implements AgentService {

    @Resource
    private AgentMapper agentMapper;

    @Autowired
    private RoleManagerService roleManagerService;

    @Autowired
    private AdministractorService administractorService;

    @Override
    public Agent getAgentByAccount(String account) {
        return agentMapper.selectByAccount(account);
    }

    @Override
    public Agent getAgentByByAdminId(Integer adminId) {
        return agentMapper.selectByPrimaryKey(adminId);
    }

    @Override
    public PageInfo<Agent> getAgentList(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize, true);
        PageHelper.orderBy("admin_id desc");
        return new PageInfo<>(agentMapper.getAgentList());
    }

    /**
     * 创建代理商
     *
     * @param agent
     */
    @Override
    @Transactional
    public int insert(Agent agent) {
        Integer adminId = 1;
        //创建管理员 并 锁定
        Administrators administrators = new Administrators();
        administrators.setLocked(LockStatus.LOCK);
        boolean result = administractorService.createAdminstrator(administrators, adminId, agent.getMobile(), agent.getLoginPassword(), agent.getOpName(), 42, null);
        if (result == false || administrators.getAdminId() == null) {
            throw new AppException(CommonCodeConst.SERVICE_ERROR, "创建失败!");
        }
        //创建关联的 代理商
        agent.setAdminId(administrators.getAdminId());
        agent.setLocked(LockStatus.LOCK);
       return agentMapper.insertSelective(agent);

    }

    @Override
    public int update(Agent agent) {
        return agentMapper.updateByPrimaryKeySelective(agent);
    }
}
