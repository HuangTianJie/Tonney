package com.gte.agent.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gop.common.Environment;
import com.gte.agent.service.AgentUserService;
import com.gte.domain.agent.AgentUser;
import com.gte.mapper.agent.AgentUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class AgentUserServieImpl implements AgentUserService {

    @Autowired
    private Environment environment;

    @Resource
    private AgentUserMapper agentUserMapper;

    /**
     * 分页查询用户基本信息
     * @param adminId 代理商ID
     * @param uid 用户ID
     * @param account 用户账号
     * @param startDate
     * @param endDate
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<AgentUser> getBaseUserListByAgent(Integer adminId,Integer uid, String account, String fullName, Date startDate, Date endDate, String orderBy, Integer pageNo,
                                                      Integer pageSize) {
        String phone = null;
        String email = null;
        if (environment.getSystemEnvironMent().equals(Environment.EnvironmentEnum.CHINA)) {
            phone = account;
        } else {
            email = account;
        }
        PageHelper.startPage(pageNo, pageSize, true);
        PageHelper.orderBy(orderBy);
        return new PageInfo<>(agentUserMapper.getUserListWithDate(adminId, uid, email,fullName,phone, startDate, endDate));
    }

}
