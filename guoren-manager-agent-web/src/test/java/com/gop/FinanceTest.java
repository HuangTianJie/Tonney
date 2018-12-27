package com.gop;

import com.google.common.collect.Lists;
import com.gop.asset.service.ConfigAssetService;
import com.gop.asset.service.FinanceService;
import com.gop.authentication.service.UserIdentificationService;
import com.gop.authentication.service.UserResidenceService;
import com.gop.coin.transfer.service.BrokerAssetOperDetailService;
import com.gop.common.ExportExcelService;
import com.gop.domain.*;
import com.gop.finance.service.UserStatisticsService;
import com.gop.match.service.MatchOrderService;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.IEmailService;
import com.gte.GuorenManagerAgentWebSpringbootApplication;
import com.gop.user.facade.UserFacade;
import com.gop.user.service.UserLoginLogService;
import com.gop.user.service.UserService;
import com.gop.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * Created by wuyanjie on 2018/4/24.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GuorenManagerAgentWebSpringbootApplication.class})
@Slf4j
public class FinanceTest {

    private static final String FILE_PATH=  "./data/";
    private static final String DOWNLOAD_PATH ="./data/";

    @Autowired
    private ConfigAssetService configAssetService;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private BrokerAssetOperDetailService brokerAssetOperDetailService;

    @Autowired
    private MatchOrderService matchOrderService;

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private UserStatisticsService userStatisticsService;



    private static final String STATUS_PATH_NAME = "./data/platAssetStatus"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    private static final String PROESS_PATH_NAME = "./data/platAssetProess"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";
    @Autowired
    private IEmailService iEmailService;

    @Value("${mail.to-user}")
    private String toUser;
    @Value("${mail.report-subject}")
    private String subject;
    @Value("${mail.report-text}")
    private String text;

    /**
     * 测试带附件的发送邮件
     */
    @Test
    public void sendAttachmentsEmail(){
//        exportPlatAsset();
        List<File> files = Lists.newArrayList(new File("userBaseList.xls"));
        //发送邮件
        EmailDto email = EmailDto.builder().toUser(Arrays.asList(toUser.split(","))).subject(subject).fileList(files).text(text).build();
        Boolean isSent = iEmailService.sendAttachmentsMail(email);
    }
    /**
     * 测试内容的发送邮件
     */
    @Test
    public void sendEmail(){
    	sendMailDirect("hfjinsong@163.com", "hello, email");
    }
    public void sendMailDirect(String toMail, String msgContent) {
		EmailDto emailDto = new EmailDto();
		emailDto.setSubject(subject);
		emailDto.setText(msgContent);

		emailDto.setToUser(Lists.newArrayList(toMail));
		iEmailService.sendSimpleMail(emailDto);

	}
    private static final String STATUS_USRE_PATH_NAME = "/data/app/multi/gdae2-exchange-manager/data/userAsset"+ DateFormatUtils.format( Calendar.getInstance().getTime(), "yyyyMMdd") + ".xls";

    @Autowired
    private UserIdentificationService userIdentificationService;
    @Autowired
    private UserResidenceService userResidenceService;
    @Test
    public void taskTest(){
        Date date = null;
        try {
            date = DateUtils.parseDate("2018-03-14 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //每天凌晨跑的是前一天的数据，记录日期为前一天
        int nativeNum = userIdentificationService.countUserLevel(date);
        int advanceNum = userResidenceService.countUserLevel(date);
        UserStatistics userStatistics = userService.getDailyStatistic(date);
        userStatistics.setNativeUser(new BigDecimal(nativeNum));
        userStatistics.setAdvancedUser(new BigDecimal(advanceNum));
        if (userStatistics.getTotalUser().compareTo(BigDecimal.ZERO) == 0) {
            userStatistics.setNativePresent(BigDecimal.ZERO);
            userStatistics.setAdvancedPresent(BigDecimal.ZERO);
        } else {
            userStatistics.setNativePresent(
                    userStatistics.getNativeUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
            userStatistics.setAdvancedPresent(
                    userStatistics.getAdvancedUser().divide(userStatistics.getTotalUser(), 4, BigDecimal.ROUND_HALF_UP));
        }
        userStatistics.setStatus(1);
        userStatistics.setCreateDate(date);
        if (userStatisticsService.countUserStatistics(date) > 0) {
            userStatisticsService.updateStatus(date);
        }
        userStatisticsService.insertStatistics(userStatistics);
    }
    @Autowired
    private UserLoginLogService userLoginLogService;

}