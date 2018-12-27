package com.gop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gop.match.service.discovery.impl.ZookeeperServiceUrlDiscovery;

@Configuration

public class ZkRegisterConfig {

	@Bean(value = "ZookeeperServiceUrlDiscovery", destroyMethod = "shutdown")
	public ZookeeperServiceUrlDiscovery getServiceUrlDiscovery() {
		ZookeeperServiceUrlDiscovery zookeeperServiceUrlDiscovery = new ZookeeperServiceUrlDiscovery();
		// zookeeperServiceUrlDiscovery.setUrls(zookeeperConfig.getNameServcers());
		// zookeeperServiceUrlDiscovery.init();
		return zookeeperServiceUrlDiscovery;
	}
}
