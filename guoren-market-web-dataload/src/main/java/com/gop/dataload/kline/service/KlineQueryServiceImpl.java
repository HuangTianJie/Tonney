 package com.gop.dataload.kline.service;

 import com.gop.dataload.kline.dto.KlineQueryDto;
 import com.gop.dataload.kline.param.KlineQueryPages;
 import com.gop.dataload.kline.param.Pages;
 import com.gop.exchange.model.KlineRecord;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Optional;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Direction;
 import org.springframework.data.domain.Sort.Order;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.BasicQuery;
 import org.springframework.stereotype.Service;

 @Slf4j
 @Service
 public class KlineQueryServiceImpl implements KlineQueryService {

   @Autowired
   private MongoTemplate mongoTemplate;

   @Override
   public KlineQueryPages getKlinePages(KlineQueryDto dto) {
     Pages<KlineRecord> pages = loadData(dto);
     //返回分页数据
     List<KlineQueryPages.KlineRecord>  records = new ArrayList<>();
     for (KlineRecord info : pages.getList()) {
       records.add(KlineQueryPages.KlineRecord.builder()
           .time(info.getTime())
           .klineType(Optional.of(info.getKlineType()).map(Enum::name).orElse(""))
           .symbol(info.getSymbol())
           .open(info.getOpen())
           .close(info.getClose())
           .maxPrice(info.getMaxPrice())
           .lowPrice(info.getLowPrice())
           .amount(info.getAmount())
           .build()
       );
     }
     return KlineQueryPages.builder()
         .pages(new Pages<>(pages.getPageNum(), pages.getPageSize(), pages.getTotal(), records))
         .build();
   }


   private Pages<KlineRecord> loadData(KlineQueryDto dto) {
     long biginTimeStamp = Timestamp.valueOf(dto.getStartTime()).getTime() / 1000;
     long endTimeStamp = Timestamp.valueOf(dto.getEndTime()).getTime() / 1000;

     String quryString = "{time:{$gte: %s,$lt:%s}}";
     BasicQuery query = new BasicQuery(String.format(quryString, biginTimeStamp, endTimeStamp));

     /**
      *
     long start = System.currentTimeMillis();
     long count = mongoTemplate.count(query, KlineRecord.class, dto.getSymbol() + ":" + dto.getKline());
     log.info("mongodb查询记录总量耗时=" + (System.currentTimeMillis() - start));
      */

     int pageNum = 1;
     long count = dto.getPageSize();
     Pages<KlineRecord> pageInfo = new Pages<>(pageNum, dto.getPageSize(), count);
     if (pageNum > pageInfo.getPages() || count == 0) {
       return pageInfo;
     }
     query.with(new Sort(new Order(Direction.DESC, "time")));
     query.skip(pageInfo.getStartIndex());
     query.limit(dto.getPageSize());
     List<KlineRecord> klineRecords = mongoTemplate.find(query, KlineRecord.class, dto.getSymbol() + ":" + dto.getKline());
     pageInfo.setList(klineRecords);
     return pageInfo;
   }

 }
