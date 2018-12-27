package com.gop.currency.transfer.task;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gop.currency.transfer.service.DepositCurrencyQueryOrderService;
import com.gop.currency.transfer.service.DepositCurrencyService;
import com.gop.domain.DepositCurrencyOrderUser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderFilterTask {

	@Autowired
	private DepositCurrencyQueryOrderService depositCurrencyQueryOrderService;
	
	@Autowired
	private DepositCurrencyService depositCurrencyService;
	
	@Scheduled(cron = "0 0/10 * * * ? ")
	public void filterOrder() {
		
		List<DepositCurrencyOrderUser> orders = depositCurrencyQueryOrderService.queryLastThrityOrder();
		
		for(DepositCurrencyOrderUser order: orders){
			try{
				depositCurrencyService.closeOrder(order.getId());
			}catch(Exception e){
				log.error("更改WAIT超过3天订单{}的状态失败:{}", order.getTxid(), e.getMessage());
			}
		}
	}
	

}
