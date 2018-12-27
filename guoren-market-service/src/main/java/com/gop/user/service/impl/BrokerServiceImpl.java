package com.gop.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.Broker;
import com.gop.exception.AppException;
import com.gop.mapper.BrokerMapper;
import com.gop.user.service.BrokerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("brokerServiceImpl")
public class BrokerServiceImpl implements BrokerService {

	@Autowired
	private BrokerMapper brokerMapper;

	@Override
	public Broker getBrokerByBrokerId(Long brokerId) {

		Broker broker = new Broker();
		try {
			broker = brokerMapper.selectByPrimaryKey(brokerId);
		} catch (Exception e) {
			log.error("根据brokerId查询broker记录异常,brokerId={},eMessage=" + e.getMessage(), brokerId, e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		return broker;
	}

	@Override
	public Broker getBrokerByMobile(String mobile) {

		Broker broker = new Broker();

		try {
			broker = brokerMapper.selectByMobile(mobile);
		} catch (Exception e) {
			log.error("根据mobile查询broker记录异常,mobile={},eMessage=" + e.getMessage(), mobile, e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		return broker;

	}

	@Override
	public Broker getBrokerByEmail(String email) {
		Broker broker = new Broker();

		try {
			broker = brokerMapper.selectByEmail(email);
		} catch (Exception e) {
			log.error("根据email查询broker记录异常,email={},eMessage=" + e.getMessage(), email, e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		return broker;

	}

	@Override
	public Long selectBrokerIdByMobile(String mobile) {

		Broker broker = new Broker();

		try {
			broker = brokerMapper.selectByMobile(mobile);
		} catch (Exception e) {
			log.error("根据mobile查询broker记录异常,mobile={},eMessage=" + e.getMessage(), mobile, e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}
		if (broker == null) {
			log.info("根据mobile查询broker记录，没有记录，mobile={}", mobile);
			return null;
		}

		Long brokerId = broker.getBrokerId();
		return brokerId;

	}

	@Override
	public Long selectBrokerIdByEmail(String email) {

		Broker broker = new Broker();

		try {
			broker = brokerMapper.selectByEmail(email);
		} catch (Exception e) {
			log.error("根据email查询broker记录异常,email={},eMessage=" + e.getMessage(), email, e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		if (broker == null) {
			log.info("根据email查询broker记录，没有记录，email={}", email);
			return null;
		}
		Long brokerId = broker.getBrokerId();
		return brokerId;

	}

	@Override
	public Boolean updateByPrimaryKeySelective(Broker broker) {
		int num = 0;
		try {
			num = brokerMapper.updateByPrimaryKeySelective(broker);
		} catch (Exception e) {
			log.error("以brokerId为主键，更新broker记录异常，brokerId={},eMessage=" + e.getMessage(), broker.getBrokerId(), e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		return (num > 0) ? true : false;

	}

	@Override
	public Boolean insertSelective(Broker broker) {
		int num = 0;

		try {
			num = brokerMapper.insertSelective(broker);
		} catch (Exception e) {
			log.error("将broker插入到Broker表异常，eMessage=" + e.getMessage(), e);
			throw new AppException(CommonCodeConst.SERVICE_ERROR);
		}

		return (num > 0) ? true : false;

	}

}
