package com.gop.asset.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gop.asset.service.FinanceHistoryService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.common.SendMessageService;
import com.gop.config.CommonConstants;
import com.gop.exception.AppException;
import com.gop.financecheck.domain.ItemFinanceHistory;
import com.gop.mode.vo.ProduceLogVo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FinanceHistoryServiceImpl implements FinanceHistoryService {
	@Value("${exchange}")
	private String exchange;

	@Value("${financeHistory.key}")
	private String financeHistoryKey;

	@Autowired
	SendMessageService sendMessageService;

	@Override
	public void sendQueueMessage(ItemFinanceHistory item) {
		Long messageId = CommonConstants.LONG_ZERO;

		try {
			if (item == null) {
				return;
			}

			ProduceLogVo produceLogVo = new ProduceLogVo();
			produceLogVo.setExchangeName(exchange);
			produceLogVo.setKey(financeHistoryKey);
			produceLogVo.setMessage(item);

			messageId = sendMessageService.tryMessage(produceLogVo);

			try {
				sendMessageService.commitMessage(messageId);
			} catch (Exception e) {
				log.error("提交财务流水消息错误时数据：", item.toString());
				log.error("提交财务流水消息错误时原因：", e);
			}
		} catch (Exception e) {
			log.error("写入财务流水时错误数据：", item.toString());
			log.error("写入财务流水时错误原因：", e);

			if (messageId > CommonConstants.LONG_ZERO) {
				sendMessageService.rollBackMessage(messageId);
			}

			throw new AppException(CommonCodeConst.SERVICE_ERROR, "写入财务流水时错误");
		}
	}

	@Override
	public void sendQueueMessage(List<ItemFinanceHistory> list) {
		List<Long> messageIds = null;

		try {
			if (list == null || list.size() <= 0) {
				return;
			}

			List<ProduceLogVo> tempList = new ArrayList<ProduceLogVo>();

			for (int i = 0; i < list.size(); i++) {
				ProduceLogVo produceLogVo = new ProduceLogVo();
				produceLogVo.setExchangeName(exchange);
				produceLogVo.setKey(financeHistoryKey);
				produceLogVo.setMessage(list.get(i));

				tempList.add(produceLogVo);
			}

			messageIds = sendMessageService.tryMessage(tempList);

			try {
				sendMessageService.commitMessage(messageIds);
			} catch (Exception e) {
				log.error("提交财务流水消息错误时数据：", list.toString());
				log.error("提交财务流水消息错误时原因：", e);
			}
		} catch (Exception e) {
			log.error("写入财务流水时错误数据：", list.toString());
			log.error("写入财务流水时错误原因：", e);

			if (messageIds != null) {
				sendMessageService.rollBackMessage(messageIds);
			}

			throw new AppException(CommonCodeConst.SERVICE_ERROR, "写入财务流水时错误");
		}
	}

	@Override
	public Long trySendQueueMessage(ItemFinanceHistory item) {
		
		ProduceLogVo produceLogVo = new ProduceLogVo();
		produceLogVo.setExchangeName(exchange);
		produceLogVo.setKey(financeHistoryKey);
		produceLogVo.setMessage(item);
		return sendMessageService.tryMessage(produceLogVo);
	}

	@Override
	public void commitSendQueueMessage(Long messageId) {
		sendMessageService.commitMessage(messageId);
	}

	@Override
	//流水先不写
	public List<Long> trySendQueueMessage(List<ItemFinanceHistory> list) {
		return null;
		/*try {
			if (list == null || list.size() <= 0) {
			
			}

			List<ProduceLogVo> tempList = new ArrayList<ProduceLogVo>();

			for (int i = 0; i < list.size(); i++) {
				ProduceLogVo produceLogVo = new ProduceLogVo();
				produceLogVo.setExchangeName(exchange);
				produceLogVo.setKey(financeHistoryKey);
				produceLogVo.setMessage(list.get(i));

				tempList.add(produceLogVo);
			}

			return sendMessageService.tryMessage(tempList);

		} catch (Exception e) {
			log.error("写入财务流水时错误数据：", list.toString());
			log.error("写入财务流水时错误原因：", e);

//			if (messageIds != null) {
//				sendMessageService.rollBackMessage(messageIds);
//			}

			throw new AppException(CommonCodeConst.SERVICE_ERROR, "写入财务流水时错误");
		}*/

	}

	@Override
	public void commitSendQueueMessage(List<Long> messageIds) {
		sendMessageService.commitMessage(messageIds);
	}

}
