package com.gop.cache;

import java.util.concurrent.TimeUnit;

public interface RedisCache {
	
	public boolean saveValue(final String phone, final String value);
	public boolean saveValue(final String phone, final String value,final Long expireTime);
	public String getValue(String phone);
	
	public void delKey(String phone);
	public void setObject(String key, Object str);
	public void setObject(String key, Object str,long timeout, TimeUnit unit);
	public Object getObject(String key);
	
	Boolean expire(String key, long timeout, TimeUnit unit);
}
