package com.gop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@SpringBootApplication
@MapperScan(basePackages={"com.gop.mapper","com.gte.mapper"})
@ComponentScan(basePackages={"com.gte","com.gop"})
@PropertySources({
		@PropertySource(value = "classpath:common.properties"),
		@PropertySource(value = "classpath:rabbitmqConfig.properties"),
		@PropertySource(value = "classpath:ulpay.properties") })
@ImportResource(locations = {"classpath:applicationContext-job.xml" })
public class GuorenManagerWebSpringbootApplication {

	public static void main(String[] args) {

		SpringApplication.run(GuorenManagerWebSpringbootApplication.class, args);
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
