package com.example.demo;

import com.gop.GdaeMarketWebSpringbootApplication;
import com.gop.sms.dto.EmailDto;
import com.gop.sms.service.IEmailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = GdaeMarketWebSpringbootApplication.class)
public class TestMail {
	@Autowired
	private IEmailService emailService;
	
	
//	@Autowired
//	@Qualifier("verifyCodeMessageGenerator")
//	private MessageGenerator<VerifyCodeDto> verifyCodeMessageGenerator;

	@Test
	public void test() {
		System.out.println("emailtest1**************");

//		String message = verifyCodeMessageGenerator.generatorMessage(environmentContxt.getSystemEnvironMent(),
//				verifyCodeDto);

		EmailDto dto = new EmailDto();
		dto.setFromUser("no-reply@GTE.com");
		List<String> list = new ArrayList<>();
		list.add("wangyang@new4g.cn");
		dto.setToUser(list);
		dto.setText("偶玛尼玛尼拜拜哄");
		dto.setSubject("subject:" + "今=͟͟͞͞天=͟͟͞͞风=͟͟͞͞好=͟͟͞͞大=͟͟͞͞啊=͟͟͞͞GTE");
		System.out.println("emailtest2**************");
		emailService.sendSimpleMail(dto);
	}

}
