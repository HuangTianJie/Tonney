package com.gte.config;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.gte.config.properties.ZookeeperConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ZookeeperConfig.class)
public class JobConfig {
	@Autowired
	private ZookeeperConfig ZookeeperConfig;

	@Bean(value="regCenterManager")
	public CoordinatorRegistryCenter createRegistryCenter() {
		CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(
				new ZookeeperConfiguration(ZookeeperConfig.getNameServcers(), "regCenterManager"));
		regCenter.init();
		return regCenter;
	}
}
