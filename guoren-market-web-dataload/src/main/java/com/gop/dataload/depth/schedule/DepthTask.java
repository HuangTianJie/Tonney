package com.gop.dataload.depth.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.gop.dataload.depth.service.Ask;
import com.gop.dataload.depth.service.Bid;
import com.gop.dataload.depth.service.DepthService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Created by Lxa on 2018/3/29.
 *
 * @author lixianan
 */
@Slf4j
@Component
public class DepthTask {

  @Autowired
  private DepthService depthService;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Scheduled(cron = "0/1 * * * * ?")
  public void depthPush() {
    try {
      log.info("push depth...");
      Set<String> keys = redisTemplate.keys("*_*:depth");
      for (String key : keys) {
        String symbol = key.split(":")[0];
        if (depthService.subscriptions(symbol) <= 0L) {
          continue;
        }
        Object buy = redisTemplate.opsForHash().get(key, "buy");
        Object sell = redisTemplate.opsForHash().get(key, "sell");
        depthService.send(symbol, parseAsk(sell), parseBid(buy));
      }
    } catch (RuntimeException e) {
      log.info("任务执行异常", e);
    }
  }

  private List<Ask> parseAsk(Object obj) {
    try {
      Assert.isInstanceOf(String.class, obj, "类型不匹配:" + obj.getClass());
      Object json = JSON.parse((String) obj);
      if (json instanceof JSONArray) {
        JSONArray items = (JSONArray) json;
        List<Ask> asks = new ArrayList<>();
        for (Object item : items) {
          Assert.isInstanceOf(JSONArray.class, item, "不能转换为数组");
          Assert.isTrue(((JSONArray) item).size() == 2, "长度错误:" + item);
          String price = ((JSONArray) item).getString(0);
          String amount = ((JSONArray) item).getString(1);
          asks.add(Ask.builder().price(price).amount(amount).build());
        }
        return asks;
      } else {
        throw new IllegalArgumentException("数据类型非法");
      }
    } catch (RuntimeException e) {
      log.error("解析卖价异常", e);
      return Lists.newArrayList();
    }
  }

  private List<Bid> parseBid(Object obj) {
    try {
      Assert.isInstanceOf(String.class, obj, "类型不匹配:" + obj.getClass());
      Object json = JSON.parse((String) obj);
      if (json instanceof JSONArray) {
        JSONArray items = (JSONArray) json;
        List<Bid> bids = new ArrayList<>();
        for (Object item : items) {
          Assert.isInstanceOf(JSONArray.class, item, "不能转换为数组");
          Assert.isTrue(((JSONArray) item).size() == 2, "长度错误:" + item);
          String price = ((JSONArray) item).getString(0);
          String amount = ((JSONArray) item).getString(1);
          bids.add(Bid.builder().price(price).amount(amount).build());
        }
        return bids;
      } else {
        throw new IllegalArgumentException("数据类型非法");
      }
    } catch (RuntimeException e) {
      log.error("解析卖价异常", e);
      return Lists.newArrayList();
    }

  }

}
