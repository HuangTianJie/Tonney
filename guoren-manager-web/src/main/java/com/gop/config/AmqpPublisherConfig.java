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
	public RabbitTemplate rabbitTemplate( @Autowired(required=true) ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(new Jackson2JsonMessageConverter());
		template.setConfirmCallback(confirmCallback);
		return template;
	}

	/**
	 * exchange
	 * 
	 * @return
	 */
	
	/*
	 * 
	 * 
	 * 		declear publis queues
	 * 
	 * 
//	 */
//	@Bean("routerGopBaoTransferOutQueue")
//	public Queue routerGopBaoTransferOutQueue() {
//		return new Queue(rabbitConfig.getRouterGopBaoTransferOutQueue(), true, false, false);
//	}
//	
////	@Bean
////	public Binding routerGopBaoTransferOutQueueBinding(@Qualifier("routerGopBaoTransferOutQueue") Queue routerGopBaoTransferOutQueue ) {
////		return BindingBuilder.bind(routerGopBaoTransferOutQueue).to(exchange())
////				.with(rabbitConfig.getRouterGopBaoTransferOutKey());
////	}
//	
//	@Bean("routerGopMarketTransferOutQueue")
//	public Queue routerGopMarketTransferOutQueue() {
//		return new Queue(rabbitConfig.getRouterGopMarketTransferOutQueue(), true, false, false);
//	}
//	
////	@Bean
////	public Binding routerGopMarketTransferOutQueueBinding(@Qualifier("routerGopMarketTransferOutQueue") Queue routerGopMarketTransferOutQueue ) {
////		return BindingBuilder.bind(routerGopMarketTransferOutQueue).to(exchange())
////				.with(rabbitConfig.getRouterGopMarketTransferOutKey());
////	}
//	
//	@Bean("financeHistoryQueue")
//	public Queue financeHistoryQueue() {
//		return new Queue(rabbitConfig.getFinanceHistoryQueue(), true, false, false);
//	}
//	
////  队列绑定由消费方操作
////	@Bean
////	public Binding blindingFinanceHistoryQueue(@Qualifier("financeHistoryQueue") Queue financeHistoryQueue ) {
////		return BindingBuilder.bind(financeHistoryQueue).to(exchange())
////				.with(rabbitConfig.getFinanceHistoryKey());
////	}
//	
//	
//	/*
//	 *
//	 * 短信邮件通道
//	 * @return
//	 */
//	
//	@Bean
//	public DirectExchange smsExchange() {
//		return new DirectExchange(rabbitConfig.getMyExchange());
//	}
//	
//	@Bean("sendSmsQueue")
//	public Queue sendSmsQueue() {
//		return new Queue(rabbitConfig.getSendSmsQueue(), true, false, false);
//	}
//	
////  队列绑定由消费方操作
////	@Bean
////	public Binding blindingSendSms(@Qualifier("sendSmsQueue") Queue sendSmsQueue ) {
////		return BindingBuilder.bind(sendSmsQueue).to(exchange())
////				.with(rabbitConfig.getSendSmsKey());
////	}
//	
//	
//	@Bean
//	public Object test(@Autowired(required = true) RabbitTemplate temp) {
//		temp.convertAndSend(rabbitConfig.getExchange(), rabbitConfig.getRouterGopBaoTransferOutQueue(), "test message");
//		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		return null;
//	}

	
	
}
