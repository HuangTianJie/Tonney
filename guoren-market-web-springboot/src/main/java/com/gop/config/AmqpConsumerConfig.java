package com.gop.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.gop.config.properties.RabbitmqConfig;

@Configuration
public class AmqpConsumerConfig implements RabbitListenerConfigurer {

	public static final Integer MQ_PREFETCH_COUNT = 2;

	@Autowired
	private RabbitmqConfig rabbitConfig;

	public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(new MappingJackson2MessageConverter());
		return factory;
	}

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());

	}

	@Bean("SimpleRabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setPrefetchCount(MQ_PREFETCH_COUNT);
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
		return factory;
	}

	/**
	 * listener
	 * 
	 * @return
	 */

	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(rabbitConfig.getExchange());
	}

	// 申明消费队列
	@Bean("transferInCoinQueue")
	public Queue transferInCoinQueue() {
		return new Queue(rabbitConfig.getTransferInCoinQueue(), true, false, false);
	}

	@Bean
	public Binding blindingTransferInQueue(@Qualifier("transferInCoinQueue") Queue transferInCoinQueue) {
		return BindingBuilder.bind(transferInCoinQueue).to(exchange()).with(rabbitConfig.getTransferInCoinKey());
	}

	@Bean
	public SimpleMessageListenerContainer TransferInCoinContainer(
			@Qualifier("transferInListener") MessageListener transferInListener, ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(transferInCoinQueue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
		container.setMessageListener(transferInListener);
		return container;
	}

	@Bean("transferOutCoinCallbackQueue")
	public Queue transferOutCoinCallbackQueue() {
		return new Queue(rabbitConfig.getTransferOutCoinReturnQueue(), true, false, false);
	}

	@Bean
	public Binding blindingTransferOutCoinCallbackQueue(
			@Qualifier("transferOutCoinCallbackQueue") Queue transferOutCoinCallbackQueue) {
		return BindingBuilder.bind(transferOutCoinCallbackQueue).to(exchange())
				.with(rabbitConfig.getTransferOutCoinReturnKey());
	}

	@Bean
	public SimpleMessageListenerContainer TransferOutCoinCallbackContainer(
			@Qualifier("transferOutCallbackListener") MessageListener transferOutCallbackListener,
			ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(transferOutCoinCallbackQueue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
		container.setMessageListener(transferOutCallbackListener);
		return container;
	}

	// 异步订单
	@Bean("matchOrderQueue")
	public Queue matchOrderQueue() {
		return new Queue(rabbitConfig.getMatchOrderQueue(), true, false, false);
	}

	@Bean("matchRecordQueue")
	public Queue matchRecordQueue() {
		return new Queue(rabbitConfig.getMatchRecordQueue(), true, false, false);
	}

	@Bean
	public Binding blindingMatchRecordQueue(@Qualifier("matchRecordQueue") Queue matchRecordQueue) {
		return BindingBuilder.bind(matchRecordQueue).to(exchange()).with(rabbitConfig.getMatchRecordKey());
	}

	@Bean
	public Binding blindingMatchOrderQueue(@Qualifier("matchOrderQueue") Queue matchOrderQueue) {
		return BindingBuilder.bind(matchOrderQueue).to(exchange()).with(rabbitConfig.getMatchOrderKey());
	}

	@Bean
	public SimpleMessageListenerContainer matchRecordContainer(
			@Qualifier("matchRecordListener") MessageListener matchRecordListener,
			ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(matchRecordQueue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
		container.setMessageListener(matchRecordListener);
		return container;
	}

	@Bean
	public SimpleMessageListenerContainer matchOrderQueueContainer(
			@Qualifier("matchOrderListener") MessageListener matchOrderListener, ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(matchOrderQueue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
		container.setMessageListener(matchOrderListener);
		return container;
	}

	@Bean("notifyQueue")
	public Queue callBackBusinessQueue() {
		return new Queue(rabbitConfig.getCallBackBusinessQueue(), true, false, false);
	}

	@Bean
	public Binding callBackBusinessQueueBinding(@Qualifier("notifyQueue") Queue callBackBusinessQueue) {
		return BindingBuilder.bind(callBackBusinessQueue).to(exchange()).with(rabbitConfig.getCallBackBusinessKey());
	}

	@Bean
	public SimpleMessageListenerContainer callBackBusinessQueueContainer(
			@Qualifier("notifyListener") MessageListener businessUrlCallbackListener,
			ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(callBackBusinessQueue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
		container.setMessageListener(businessUrlCallbackListener);
		return container;
	}

}
