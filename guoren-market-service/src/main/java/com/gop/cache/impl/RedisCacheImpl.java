package com.gop.cache.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.gop.cache.RedisCache;

@Repository("redisCache")
@SuppressWarnings("unchecked")
public class RedisCacheImpl implements RedisCache {
	private static final Logger log = LoggerFactory
			.getLogger(RedisCacheImpl.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public boolean saveValue(String phone, String value) {
		String key = Joiner.on(":").join("sms.send.count:userphone", phone);
		try {
			stringRedisTemplate.opsForValue().set(key, value);
		} catch (Exception e) {
			log.error("redis error,key:{},value:{}", key, value, e);
			return false;
		}
		return true;
	}

	@Override
	public String getValue(String phone) {
		String value = "";
		String key = Joiner.on(":").join("sms.send.count:userphone", phone);
		try {
			value = Optional.fromNullable(
					stringRedisTemplate.opsForValue().get(key)).or("0");
		} catch (Exception e) {
			log.error("redis error: key {}", key, e);
		}
		return value;
	}

	@Override
	public void delKey(String phone) {
		String key = Joiner.on(":").join("sms.send.count:userphone", phone);
		try {
			stringRedisTemplate.delete(key);
		} catch (Exception e) {
			log.error("redis error: key {}", key, e);
		}

	}

	/**
	 * 可设置过期时间，单位秒
	 */
	@Override
	public boolean saveValue(String phone, String value, Long expireTime) {
		String key = Joiner.on(":").join("sms.send.count:userphone", phone);
		try {
			stringRedisTemplate.opsForValue().set(key, value);
			stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("redis error,key:{},value:{}", key, value, e);
			return false;
		}
		return true;
	}

	
	@Override
	public void setObject(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Override
	public Object getObject(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public Boolean expire(String key, long timeout, TimeUnit unit) {

		return redisTemplate.expire(key, timeout, unit);
	}

	@Override
	public void setObject(String key, Object value, long timeout, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, timeout, unit);
		
	}
}
