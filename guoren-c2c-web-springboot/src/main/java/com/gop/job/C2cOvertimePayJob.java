package com.gop.job;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.google.common.collect.Lists;
import com.gop.c2c.facade.C2cMessageFacade;
import com.gop.c2c.service.C2cTransOrderOvertimeRecordService;
import com.gop.c2c.service.C2cTransOrderService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.MessageConst;
import com.gop.common.Environment;
import com.gop.domain.C2cTransOrder;
import com.gop.domain.C2cTransOrderOvertimeRecord;
import com.gop.domain.enums.C2cTransOrderStatus;
import com.gop.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("C2cOvertimePayJob")
public class C2cOvertimePayJob implements SimpleJob {

	@Autowired
	C2cTransOrderOvertimeRecordService c2cTransOrderOvertimeRecordService;
	@Autowired
	C2cTransOrderService c2cTransOrderService;
	@Autowired
	C2cMessageFacade c2cMessageFacade;
	@Autowired
	Environment environmentContxt;

	@Override
	@Transactional
	public void execute(ShardingContext shardingContext) {
		List<C2cTransOrder> list = Lists.newArrayList();
		list = c2cTransOrderService.selectByStatus(C2cTransOrderStatus.UNPAY);
		Calendar currentDayBegin = new GregorianCalendar();
		currentDayBegin.set(Calendar.HOUR_OF_DAY, 0);
		currentDayBegin.set(Calendar.MINUTE, 0);
		currentDayBegin.set(Calendar.SECOND, 0);
		for (C2cTransOrder c2cTransOrder : list) {
			// 当天时间超时脏数据校验
			// 大于(未付款时间+三十分钟),大于当天零时,并且是未付款状态的
			if (((new Date().getTime() - c2cTransOrder.getCreateDate().getTime()) > 30 * 60 * 1000)
					&&(c2cTransOrder.getCreateDate().getTime()> currentDayBegin.getTimeInMillis())
					&&(c2cTransOrder.getStatus().equals(C2cTransOrderStatus.UNPAY))
					) {
				// 超时更新
				c2cTransOrderService.updateTransOrderToOverTime(c2cTransOrder.getTransOrderId(),
						c2cTransOrder.getBuyUid(), c2cTransOrder.getBuyUid(), "");
			}
//			// 小于30分钟,大于25分钟
//			if ((new Date().getTime() - c2cTransOrder.getCreateDate().getTime()) > 25 * 60 * 1000
//					&& (new Date().getTime() - c2cTransOrder.getCreateDate().getTime()) < 30 * 60 * 1000) {
//				// 催促提醒
//				String msg = String.format(MessageConst.C2C_BUY_BEFORE_FIVE_MINUTES_OVERTIME_MESSAGE,
//						c2cTransOrder.getAssetCode(), c2cTransOrder.getTransOrderId(),
//						c2cTransOrder.getMoney().toString(), c2cTransOrder.getNumber().toString(), c2cTransOrder.getAssetCode());
//				c2cMessageFacade.sendMessageByUid(c2cTransOrder.getBuyUid(), msg);
//				C2cTransOrderOvertimeRecord record = new C2cTransOrderOvertimeRecord();
//				record.setTransOrderId(c2cTransOrder.getTransOrderId());
//				Integer result = c2cTransOrderOvertimeRecordService.saveRemind(record);
//				if (!(result == 1)) {
//					throw new AppException(CommonCodeConst.SERVICE_ERROR, "保存超时订单信息提醒记录失败");
//				}
//			}
		}
	}

}
