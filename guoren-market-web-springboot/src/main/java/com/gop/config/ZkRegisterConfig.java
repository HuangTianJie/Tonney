package com.gop.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gop.config.properties.ZookeeperConfig;
import com.gop.match.service.discovery.impl.ZookeeperServiceUrlDiscovery;

 

@Configuration
@EnableConfigurationProperties(ZookeeperConfig.class)
public class ZkRegisterConfig {
	
	@Autowired
	ZookeeperConfig zookeeperConfig;
	

	
	@Bean(value="ZookeeperServiceUrlDiscovery",destroyMethod="shutdown")
	public ZookeeperServiceUrlDiscovery getServiceUrlDiscovery() {
		ZookeeperServiceUrlDiscovery zookeeperServiceUrlDiscovery = new ZookeeperServiceUrlDiscovery();
		zookeeperServiceUrlDiscovery.setUrls(zookeeperConfig.getNameServcers());
    	zookeeperServiceUrlDiscovery.init();
		return zookeeperServiceUrlDiscovery;
	}
}
