package com.gop.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

//@ConfigurationProperties(locations = "classpath:config/rabbitmqConfig.properties")
@Data
@Component
public class RabbitmqConfig {

	@Value("${exchange}")
	private String exchange;
	
	/*
	 * new rabbitmq queue
	 */
	@Value("${matchOrder.queue}")
	private String matchOrderQueue;

	@Value("${matchOrder.key}")
	private String matchOrderKey;

	@Value("${matchRecord.queue}")
	private String matchRecordQueue;

	@Value("${matchRecord.key}")
	private String matchRecordKey;
	
	@Value("${transferOut.btc.key}")
	private String transferOutBtcKey;

	@Value("${transferOut.gop.key}")
	private String transferOutGopKey;
	
	@Value("${transferIn.coin.key}")
	private String transferInCoinKey;

	@Value("${transferIn.coin.queue}")
	private String transferInCoinQueue;
	
	@Value("${transferOut.coin.return.key}")
	private String transferOutCoinReturnKey;

	@Value("${transferOut.coin.return.queue}")
	private String transferOutCoinReturnQueue;
	
	@Value("${callBackBusiness.queue}")
	private String callBackBusinessQueue;

	@Value("${callBackBusiness.key}")
	private String callBackBusinessKey;

//	@Value("${notify.key}")
//	private String notifyKey;
//	
//	@Value("${notify.queue}")
//	private String notifyQueue;


//	
//	transferIn.coin.key
	

//	@Value("${individual.gop.transferOut.queue}")
//	private String individualGopTransferOutQueue;
//
//	@Value("${individual.gop.transferOut.key}")
//	private String individualGopTransferOutKey;
//
//	@Value("${individual.gop.transferIn.queue}")
//	private String individualGopTransferInQueue;
//
//	@Value("${individual.gop.transferIn.key}")
//	private String individualGopTransferInKey;
//
//	@Value("${business.gop.transferOut.queue}")
//	private String businessGopTransferOutQueue;
//
//	@Value("${business.gop.transferOut.key}")
//	private String businessGopTransferOutKey;
//
//	@Value("${business.gop.transferIn.queue}")
//	private String businessGopTransferInQueue;
//
//	@Value("${business.gop.transferIn.key}")
//	private String businessGopTransferInKey;
//
//	@Value("${router.gop.bao.transferOutReturn.queue}")
//	private String routerGopBaoTransferOutReturnQueue;
//
//	@Value("${router.gop.bao.transferOutReturn.key}")
//	private String routerGopBaoTransferOutReturnKey;
//
//	@Value("${router.gop.bao.transferIn.queue}")
//	private String routerGopBaoTransferInQueue;
//
//	@Value("${router.gop.bao.transferIn.key}")
//	private String routerGopBaoTransferInKey;
//
//	@Value("${router.gop.market.transferOutReturn.queue}")
//	private String routerGopMarketTransferOutReturnQueue;
//
//	@Value("${router.gop.market.transferOutReturn.key}")
//	private String routerGopMarketTransferOutReturnKey;
//
//	@Value("${router.gop.market.transferIn.queue}")
//	private String routerGopMarketTransferInQueue;
//
//	@Value("${router.gop.market.transferIn.key}")
//	private String routerGopMarketTransferInKey;
//
//	@Value("${individual.matchRecord.queue}")
//	private String individualMatchRecordQueue;
//
//	@Value("${individual.matchRecord.key}")
//	private String individualMatchRecordKey;
//
//	@Value("${business.matchRecord.queue}")
//	private String businessMatchRecordQueue;
//
//	@Value("${business.matchRecord.key}")
//	private String businessMatchRecordKey;
//

	// 短信通道
	@Value("${myExchange}")
	private String myExchange;

//	// 消费队列配置
//	@Value("${router.gop.bao.transferOut.queue}")
//	private String routerGopBaoTransferOutQueue;
//
//	// 消费队列配置
//	@Value("${router.gop.bao.transferOut.key}")
//	private String routerGopBaoTransferOutKey;
//	
//
//	@Value("${router.gop.market.transferOut.queue}")
//	private String routerGopMarketTransferOutQueue;
//
//	@Value("${router.gop.market.transferOut.key}")
//	private String routerGopMarketTransferOutKey;

	@Value("${financeHistory.queue}")
	private String financeHistoryQueue;

	@Value("${financeHistory.key}")
	private String financeHistoryKey;

	@Value("${sendSms.queue}")
	private String sendSmsQueue;

	@Value("${sendSms.key}")
	private String sendSmsKey;

}
