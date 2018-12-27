package com.gop;


import com.gop.dataload.kline.dto.KlineQueryDto;
import com.gop.dataload.kline.param.KlineQueryPages;
import com.gop.dataload.kline.service.KlineQueryService;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DataLoadApplicationTests {

  @Autowired
  private KlineQueryService klineQueryService;

  @Test
  public void contextLoads() {
  }

  @Test
  public void testKlinePages() {
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(90L);
    KlineQueryDto dto = KlineQueryDto.builder()
        .startTime(start)
        .endTime(end)
        //.pageNum(1)
        .pageSize(20)
        .symbol("BTC_ACT")
        .kline("MIN15")
        .build();
    KlineQueryPages klinePages = klineQueryService.getKlinePages(dto);
    System.out.println(klinePages);
  }
}
