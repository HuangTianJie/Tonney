package com.gop.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gop.config.properties.RabbitmqConfig;

@Configuration
public class AmqpPublisherConfig {

	public static final Integer MQ_PREFETCH_COUNT = 5;

	@Autowired
	private RabbitmqConfig rabbitConfig;
	@Autowired
	@Qualifier("callBack")
	private ConfirmCallback confirmCallback;

	@Bean
	public RabbitTemplate rabbitTemplate(@Autowired(required = true) ConnectionFactory connectionFactory) {

		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(new Jackson2JsonMessageConverter());
		template.setConfirmCallback(confirmCallback);
		return template;
	}

}
