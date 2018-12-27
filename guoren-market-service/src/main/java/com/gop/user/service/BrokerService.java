package com.gop.user.service;

import com.gop.domain.Broker;

/**
 * 券商相关
 * 
 * @author liuze
 *
 */
public interface BrokerService {

	//根据券商id获取券商记录
	public Broker getBrokerByBrokerId(Long brokerId);

	//根据手机号查询券商记录
	public Broker getBrokerByMobile(String mobile);

	//根据邮箱查询券商记录
	public Broker getBrokerByEmail(String email);

	//根据手机获取券商id
	public Long selectBrokerIdByMobile(String phone);

	//根据邮箱获取券商id
	public Long selectBrokerIdByEmail(String email);

	//根据券商id更新券商信息
	public Boolean updateByPrimaryKeySelective(Broker broker);
	
	//插入券商信息
	public Boolean insertSelective(Broker broker);

}
