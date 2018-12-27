package com.gop.task.domestic;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gop.common.Environment;
import com.gop.common.Environment.EnvironmentEnum;
import com.gop.currency.withdraw.gateway.service.cibpay.service.CibBankService;
import com.gop.currency.withdraw.gateway.service.cibpay.service.impl.DownloadServiceImpl;
import com.gop.util.ZipUtil;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CibBankTask {
	@Autowired
	@Qualifier("cibBankService")
	CibBankService cibBankService;
	
	@Autowired
	private Environment environmentContxt;

	@Scheduled(cron = "0 0 0 * * ?  ") // 每天执行一次
	public void doupdate() throws Exception {
		if (EnvironmentEnum.CHINA.equals(environmentContxt.getSystemEnvironMent())) {
			HashMap<String, String> params = new HashMap<String, String>();
	        params.put("download_type", "01");
			Object obj = new DownloadServiceImpl().download(params);
			if (obj instanceof String) {
				log.error("银行代码下载异常{}", obj);
			} else {
				String cibObjectsString = ZipUtil.unZipOneTxt((byte[]) obj);
				log.info(cibObjectsString);
				cibBankService.savaCibBankByString(cibObjectsString);
			}
			log.info("下载银行代码定时任务结束");
		}
	}
 
}
