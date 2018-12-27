package com.gop.asset.service;

import java.util.List;

import com.gop.financecheck.domain.ItemFinanceHistory;


/**
 * 财务流水接口
 * 
 * @author kennghuang
 *
 */
public interface FinanceHistoryService {
	/**
	 * 发送财务流水消息
	 */
	@Deprecated
	public void sendQueueMessage(ItemFinanceHistory item);
	
	/**
	 * 发送财务流水消息
	 */
	@Deprecated
	public void sendQueueMessage(List<ItemFinanceHistory> list);
	
	public Long trySendQueueMessage(ItemFinanceHistory item);
	
	public void commitSendQueueMessage(Long messageId);
	
	public List<Long> trySendQueueMessage(List<ItemFinanceHistory> list);
	
	public void commitSendQueueMessage(List<Long> messageIds);
	
}
