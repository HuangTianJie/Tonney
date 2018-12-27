package com.gte.config;

import com.gte.config.properties.ZookeeperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gop.match.service.discovery.impl.ZookeeperServiceUrlDiscovery;

@Configuration
@EnableConfigurationProperties(ZookeeperConfig.class)
public class ZkRegisterConfig {
	
	@Autowired
	private ZookeeperConfig zookeeperConfig;

	@Bean(value = "ZookeeperServiceUrlDiscovery",destroyMethod="shutdown")
	public ZookeeperServiceUrlDiscovery getServiceUrlDiscovery() {
		ZookeeperServiceUrlDiscovery zookeeperServiceUrlDiscovery = new ZookeeperServiceUrlDiscovery();
		zookeeperServiceUrlDiscovery.setUrls(zookeeperConfig.getNameServcers());
		zookeeperServiceUrlDiscovery.init();
		return zookeeperServiceUrlDiscovery;
	}
}
