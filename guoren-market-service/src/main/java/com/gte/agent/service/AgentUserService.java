/*
 * 文件名：UserService.java
 * 版权：Copyright by www.guoren.com
 * 描述：
 * 修改人：zhangxianglong
 * 修改时间：2016年3月8日
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */

package com.gte.agent.service;

import com.github.pagehelper.PageInfo;
import com.gte.domain.agent.AgentUser;

import java.util.Date;

public interface AgentUserService {

    /**
     * 分页查询用户基本信息
     * @param aid 代理商ID
     * @param uid 用户ID
     * @param account 用户账号
     * @param startDate
     * @param endDate
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageInfo<AgentUser> getBaseUserListByAgent(Integer aid, Integer uid, String account, String fullName, Date startDate, Date endDate, String orderBy, Integer pageNo,
                                                      Integer pageSize) ;

}
