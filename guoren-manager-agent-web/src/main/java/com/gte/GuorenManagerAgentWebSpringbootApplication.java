package com.gte;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@SpringBootApplication
@MapperScan(basePackages={"com.gop.mapper","com.gte.mapper"})
@ComponentScan(basePackages={"com.gte","com.gop"})
@PropertySources({
		@PropertySource(value = "classpath:common.properties"),
		@PropertySource(value = "classpath:rabbitmqConfig.properties"),
		@PropertySource(value = "classpath:ulpay.properties") })
@ImportResource(locations = {"classpath:applicationContext-job.xml" })
public class GuorenManagerAgentWebSpringbootApplication {

	public static void main(String[] args) {

		SpringApplication.run(GuorenManagerAgentWebSpringbootApplication.class, args);
	}

	//配置freemarker生产excel文件，加载的模版路径
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		String templateLoaderPath="classpath:freemarker/";
		configurer.setTemplateLoaderPath(templateLoaderPath);
		return configurer;
	}
}
