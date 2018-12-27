package com.gop.dataload.kline.controller;

import com.google.common.collect.ImmutableMap;
import com.gop.code.consts.CommonCodeConst;
import com.gop.dataload.kline.dto.KlineQueryDto;
import com.gop.dataload.kline.param.KlineQueryReq;
import com.gop.exception.AppException;
import com.gop.dataload.kline.param.KlineQueryPages;
import com.gop.dataload.kline.service.KlineQueryService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Lxa on 2018/1/30.
 *
 * @author lixianan
 */
@Slf4j
@RestController
@RequestMapping("/kline-query")
public class KlineQueryController {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final Map<String, String> klins = new ImmutableMap.Builder<String, String>().put("1m", "MIN1").put("5m", "MIN5").put("15m", "MIN15").put("30m", "MIN30").put("1h", "MIN60").put("1d", "DAY").build();

  @Autowired
  private KlineQueryService klineQueryService;

  @GetMapping("/pages")
  public KlineQueryPages getKlinePages(KlineQueryReq req) {
    log.info("查询k线数据：" + req);
    KlineQueryDto dto = checkAndSet(req);
    long start = System.currentTimeMillis();
    KlineQueryPages klineQueryPages;
    try {
      klineQueryPages = klineQueryService.getKlinePages(dto);
      log.info("查询k线数据完成，耗时：" + (System.currentTimeMillis() - start));
      return klineQueryPages;
    } catch (RuntimeException e) {
      log.info("查询k线数据异常，耗时：" + (System.currentTimeMillis() - start));
      throw e;
    }
  }

  private KlineQueryDto checkAndSet(KlineQueryReq req) {
    LocalDateTime start;
    try {
      start = new Date(req.getStartTime()*1000).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (Exception e) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "开始时间非法");
    }
    LocalDateTime end;
    try {
      end = new Date(req.getEndTime()*1000).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (Exception e) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "结束时间非法");
    }
    if (start.isBefore(end.minusYears(1L))) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "只能查询最近一年的记录");
    }
    if (req.getPageSize() < 1 || req.getPageSize() > 300) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "条数非法:" + req.getPageSize());
    }
    if (StringUtils.isEmpty(req.getSymbol())) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "交易对非法:" + req.getSymbol());
    }
    if (StringUtils.isEmpty(klins.get(req.getKline()))) {
      throw new AppException(CommonCodeConst.FIELD_ERROR, "K线类型非法:" + req.getKline());
    }
    return KlineQueryDto.builder()
        .startTime(start)
        .endTime(end)
        //.pageNum(req.getPageNum())
        .pageSize(req.getPageSize())
        .symbol(req.getSymbol())
        .kline(klins.get(req.getKline()))
        .build();
  }

}
