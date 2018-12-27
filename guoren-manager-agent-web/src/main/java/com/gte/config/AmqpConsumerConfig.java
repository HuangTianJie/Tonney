package com.gte.config;

import com.gte.config.properties.RabbitmqConfig;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
public class AmqpConsumerConfig implements RabbitListenerConfigurer {

	public static final Integer MQ_PREFETCH_COUNT = 5;

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

//	/**
//	 * listener
//	 * 
//	 * @return
//	 */
//	
//	
//	@Bean
//	public DirectExchange exchange() {
//		return new DirectExchange(rabbitConfig.getExchange());
//	}
//	
//	
//	//申明消费队列
//	@Bean("matchRecordQueue")
//	public Queue matchRecordQueue() {
//		return new Queue(rabbitConfig.getMatchRecordQueue(), true, false, false);
//	}
//
//	//绑定到rabbitmq的exchange
//	@Bean
//	public Binding blindingMatchRecordQueue(@Qualifier("matchRecordQueue") Queue matchRecordQueue ) {
//		return BindingBuilder.bind(matchRecordQueue).to(exchange())
//				.with(rabbitConfig.getMatchRecordKey());
//	}
//	
//	//设置消费队列
//	@Bean
//	public SimpleMessageListenerContainer matchRecordContainer(MessageListener matchRecordServiceListener,
//			ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(matchRecordQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(matchRecordServiceListener);
//		return container;
//	}
//	
//	@Bean("routerGopMarketTransferOutReturnQueue")
//	public Queue routerGopMarketTransferOutReturnQueue() {
//		return new Queue(rabbitConfig.getRouterGopMarketTransferOutReturnQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingRouterGopMarketTransferOutReturnQueue(@Qualifier("routerGopMarketTransferOutReturnQueue") Queue routerGopMarketTransferOutReturnQueue ) {
//		return BindingBuilder.bind(routerGopMarketTransferOutReturnQueue).to(exchange())
//				.with(rabbitConfig.getRouterGopMarketTransferOutReturnKey());
//	}
//	
//	
//	@Bean("routerGopBaoTransferOutReturnQueue")
//	public Queue routerGopBaoTransferOutReturnQueue() {
//		return new Queue(rabbitConfig.getRouterGopBaoTransferOutReturnQueue(), true, false, false);
//	}
//	
//
//	@Bean
//	public Binding blindingRouterGopBaoTransferOutReturnQueue(@Qualifier("routerGopBaoTransferOutReturnQueue") Queue routerGopBaoTransferOutReturnQueue ) {
//		return BindingBuilder.bind(routerGopBaoTransferOutReturnQueue).to(exchange())
//				.with(rabbitConfig.getRouterGopBaoTransferOutReturnKey());
//	}
//	
//	
//	@Bean
//	public SimpleMessageListenerContainer transferGopResultContainer(MessageListener transferGopResultListener,
//			ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(routerGopMarketTransferOutReturnQueue(), routerGopBaoTransferOutReturnQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(transferGopResultListener);
//		return container;
//	}
//
//	
//	
//	@Bean("routerGopMarketTransferInQueue")
//	public Queue routerGopMarketTransferInQueue() {
//		return new Queue(rabbitConfig.getRouterGopMarketTransferInQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingRouterGopMarketTransferInQueue(@Qualifier("routerGopMarketTransferInQueue") Queue routerGopMarketTransferInQueue ) {
//		return BindingBuilder.bind(routerGopMarketTransferInQueue).to(exchange())
//				.with(rabbitConfig.getRouterGopMarketTransferInKey());
//	}
//
//	@Bean("routerGopBaoTransferInQueue")
//	public Queue routerGopBaoTransferInQueue() {
//		return new Queue(rabbitConfig.getRouterGopBaoTransferInQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingRouterGopBaoTransferInQueue(@Qualifier("routerGopBaoTransferInQueue") Queue routerGopBaoTransferInQueue ) {
//		return BindingBuilder.bind(routerGopBaoTransferInQueue).to(exchange())
//				.with(rabbitConfig.getRouterGopBaoTransferInKey());
//	}
//
//
//	@Bean
//	public SimpleMessageListenerContainer transferInGopContainer(MessageListener transferInGopListener,
//			ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(routerGopMarketTransferInQueue(), routerGopBaoTransferInQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(transferInGopListener);
//		return container;
//	}
//
//	@Bean("businessGopTransferOutQueue")
//	public Queue businessGopTransferOutQueue() {
//		return new Queue(rabbitConfig.getBusinessGopTransferOutQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding businessGopTransferOutQueueBinding(@Qualifier("businessGopTransferOutQueue") Queue businessGopTransferOutQueue ) {
//		return BindingBuilder.bind(businessGopTransferOutQueue).to(exchange())
//				.with(rabbitConfig.getBusinessGopTransferOutKey());
//	}
//
//	@Bean("individualGopTransferOutQueue")
//	public Queue individualGopTransferOutQueue() {
//		return new Queue(rabbitConfig.getIndividualGopTransferOutQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding individualGopTransferOutQueueBinding(@Qualifier("individualGopTransferOutQueue") Queue individualGopTransferOutQueue ) {
//		return BindingBuilder.bind(individualGopTransferOutQueue).to(exchange())
//				.with(rabbitConfig.getIndividualGopTransferOutKey());
//	}
//
//	@Bean
//	public SimpleMessageListenerContainer businessGopTransferOutQueueContainer(MessageListener transferOutGopListener,
//			ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(businessGopTransferOutQueue(), individualGopTransferOutQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(transferOutGopListener);
//		return container;
//	}
//
//	@Bean("businessGopTransferInQueue")
//	public Queue businessGopTransferInQueue() {
//		return new Queue(rabbitConfig.getBusinessGopTransferInQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding businessGopTransferInQueueBinding(@Qualifier("businessGopTransferInQueue") Queue businessGopTransferInQueue ) {
//		return BindingBuilder.bind(businessGopTransferInQueue).to(exchange())
//				.with(rabbitConfig.getBusinessGopTransferInKey());
//	}
//	
//	@Bean
//	public SimpleMessageListenerContainer businessGopTransferInQueueContainer(
//			MessageListener businessTransferGopInListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(businessGopTransferInQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(businessTransferGopInListener);
//		return container;
//	}
//	
//	
//	@Bean("individualGopTransferInQueue")
//	public Queue individualGopTransferInQueue() {
//		return new Queue(rabbitConfig.getIndividualGopTransferInQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding individualGopTransferInQueueBinding(@Qualifier("individualGopTransferInQueue") Queue individualGopTransferInQueue ) {
//		return BindingBuilder.bind(individualGopTransferInQueue).to(exchange())
//				.with(rabbitConfig.getIndividualGopTransferInKey());
//	}
//
//	@Bean
//	public SimpleMessageListenerContainer individualGopTransferInQueueContainer(
//			MessageListener individualTransferGopInListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(individualGopTransferInQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(individualTransferGopInListener);
//		return container;
//	}
//
//	@Bean("callBackBusinessQueue")
//	public Queue callBackBusinessQueue() {
//		return new Queue(rabbitConfig.getCallBackBusinessQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding callBackBusinessQueueBinding(@Qualifier("callBackBusinessQueue") Queue callBackBusinessQueue ) {
//		return BindingBuilder.bind(callBackBusinessQueue).to(exchange())
//				.with(rabbitConfig.getCallBackBusinessKey());
//	}	
//
//	@Bean
//	public SimpleMessageListenerContainer callBackBusinessQueueContainer(
//			MessageListener businessUrlCallbackListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(callBackBusinessQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(businessUrlCallbackListener);
//		return container;
//	}
//
//	
//	@Bean("matchOrderQueue")
//	public Queue matchOrderQueue() {
//		return new Queue(rabbitConfig.getMatchOrderQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingMatchOrderQueue(@Qualifier("matchOrderQueue") Queue matchOrderQueue ) {
//		return BindingBuilder.bind(matchOrderQueue).to(exchange())
//				.with(rabbitConfig.getMatchOrderKey());
//	}
//	
//	@Bean
//	public SimpleMessageListenerContainer matchOrderQueueContainer(
//			MessageListener businessOrderListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(matchOrderQueue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(businessOrderListener);
//		return container;
//	}
//	
//	@Bean("businessMatchRecord")
//	public Queue businessMatchRecord() {
//		return new Queue(rabbitConfig.getBusinessMatchRecordQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingBusinessMatchRecord(@Qualifier("businessMatchRecord") Queue businessMatchRecord ) {
//		return BindingBuilder.bind(businessMatchRecord).to(exchange())
//				.with(rabbitConfig.getBusinessMatchRecordKey());
//	}
//	
//	@Bean
//	public SimpleMessageListenerContainer businessMatchRecordQueueContainer(
//			MessageListener businessMatchBackServiceListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(businessMatchRecord());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(businessMatchBackServiceListener);
//		return container;
//	}
//	
//	@Bean("individualMatchRecord")
//	public Queue individualMatchRecord() {
//		return new Queue(rabbitConfig.getIndividualMatchRecordQueue(), true, false, false);
//	}
//	
//	@Bean
//	public Binding blindingIndividualMatchRecord(@Qualifier("individualMatchRecord") Queue individualMatchRecord ) {
//		return BindingBuilder.bind(individualMatchRecord).to(exchange())
//				.with(rabbitConfig.getIndividualMatchRecordKey());
//	}
//	
//	@Bean
//	public SimpleMessageListenerContainer individualMatchRecordQueueContainer(
//			MessageListener indivdualMatchBackServiceListener, ConnectionFactory connectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//		container.setQueues(individualMatchRecord());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(1);
//		container.setConcurrentConsumers(1);
//		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 设置确认模式手工确认
//		container.setMessageListener(indivdualMatchBackServiceListener);
//		return container;
//	}
}
