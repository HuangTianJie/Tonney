package com.gop.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.google.common.collect.Lists;
import com.gop.config.MailConfig;
import com.gop.domain.EmailLog;
import com.gop.domain.enums.ServiceCode;
import com.gop.domain.enums.ServiceProvider;
import com.gop.domain.enums.SysCode;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.EmailLogService;
import com.gop.sms.service.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by YAO on 2018/5/29.
 */

@Slf4j
@Component("sendReportJob")
@EnableConfigurationProperties(MailConfig.class)
public class SendReportJob implements SimpleJob {
  @Autowired
  private MailConfig mailConfig;
  @Autowired
  private IEmailService iEmailService;
  @Autowired
  private EmailLogService emailLogService;


  @Override
  @Transactional
  public void execute(ShardingContext shardingContext) {
    String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");

    String STATUS_USRE_PATH_NAME = mailConfig.getReportRoot() + "userAsset" + fileDate + ".xls";
    String STATUS_PATH_NAME = mailConfig.getReportRoot() + "platAssetStatus"+ fileDate + ".xls";
    String PROESS_PATH_NAME = mailConfig.getReportRoot() + "platAssetProess"+ fileDate + ".xls";
    String WALLET_PATH_NAME = mailConfig.getReportRoot() + "walletBalance"+ fileDate + ".xls";
    String INOUT_PATH_NAME = mailConfig.getReportRoot() + "walletInOut"+ fileDate + ".xls";
    String TOTAL_PATH_NAME = mailConfig.getReportRoot() + "total"+ fileDate + ".xls";

    File platProess = new File(PROESS_PATH_NAME);
    File platStatus = new File(STATUS_PATH_NAME);
    File userStatus = new File(STATUS_USRE_PATH_NAME);
    File walletBalance = new File(WALLET_PATH_NAME);
    File walletInout = new File(INOUT_PATH_NAME);
    File total = new File(TOTAL_PATH_NAME);

    List<File> files = new ArrayList<>();
    if (platStatus.exists()){
      files.add(platStatus);
    }
    if (platProess.exists()){
      files.add(platProess);
    }
    if (userStatus.exists()){
      files.add(userStatus);
    }
    if (walletBalance.exists()){
      files.add(walletBalance);
    }
    if (walletInout.exists()){
      files.add(walletInout);
    }
    if (total.exists()){
      files.add(total);
    }
    //发送邮件
    EmailDto
        email = EmailDto.builder().toUser(Lists.newArrayList(mailConfig.getToUser().split(","))).subject(mailConfig.getReportSubject()).text(mailConfig.getReportText()).fileList(files).build();
    Boolean isSent = iEmailService.sendAttachmentsMail(email);
    buildEmailLog(new Date(), "财务日报表", isSent);
  }

  private void buildEmailLog(Date reportDate,String content, Boolean isSent) {
    EmailLog emailLog = new EmailLog();
    emailLog.setMsgId("");// msgId 短信服务商返回的信息；
    emailLog.setSysCode(SysCode.GTE_MANAGER);
    emailLog.setServiceCode(ServiceCode.REPORT_DAILY);
    emailLog.setServiceProvider(ServiceProvider.TENCENT);
    emailLog.setMsgContent(reportDate +" "+content +" "+ (isSent?"发送成功":"发送失败"));
    emailLog.setEmail(mailConfig.getToUser()); // 需要传送 邮箱发送地址
    emailLog.setCreateDate(new Date());
    emailLogService.addEmailLog(emailLog);
  }
}
