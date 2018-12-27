package com.gte.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

//@ConfigurationProperties(locations = "classpath:rabbitmqConfig.properties")
@Data
@Component
public class RabbitmqConfig {

	@Value("${exchange}")
	private String exchange;

	@Value("${individual.gop.transferOut.queue}")
	private String individualGopTransferOutQueue;

	@Value("${individual.gop.transferOut.key}")
	private String individualGopTransferOutKey;

	@Value("${business.gop.transferOut.queue}")
	private String businessGopTransferOutQueue;

	@Value("${business.gop.transferOut.key}")
	private String businessGopTransferOutKey;

	@Value("${callBackBusiness.queue}")
	private String callBackBusinessQueue;

	@Value("${callBackBusiness.key}")
	private String callBackBusinessKey;

	@Value("${sendSms.queue}")
	private String sendSmsQueue;
	
	@Value("${sendSms.key}")
	private String sendSmsKey;

	@Value("${financeHistory.queue}")
	private String financeHistoryQueue;

	@Value("${financeHistory.key}")
	private String financeHistoryKey;

}
