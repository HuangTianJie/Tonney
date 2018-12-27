package com.gop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@ImportResource(locations = { "classpath:applicationContext-*.xml" })
@MapperScan(basePackages={"com.gop.mapper","com.gte.mapper"})
@ComponentScan(basePackages={"com.gte","com.gop"})
@PropertySources({ @PropertySource("classpath:apiresource.properties"), @PropertySource("classpath:common.properties"),
		@PropertySource("classpath:rabbitmqConfig.properties") })
@EnableCaching
public class GdaeC2CWebSpringbootApplication {

	public static void main(String[] args) {

		SpringApplication.run(GdaeC2CWebSpringbootApplication.class, args);
	}
}
