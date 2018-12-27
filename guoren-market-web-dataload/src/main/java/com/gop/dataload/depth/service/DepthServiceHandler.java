package com.gop.dataload.depth.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Created by Lxa on 2018/3/28.
 *
 * @author lixianan
 */
@Slf4j
public class DepthServiceHandler extends TextWebSocketHandler implements DepthService {

  private static final ExecutorService service = Executors.newCachedThreadPool();

  private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("Websocket连接已建立:" + session.getId());
    sessions.add(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    log.info("Websocket连接已关闭:" + session.getId());
    sessions.remove(session);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
    log.error("连接出现错误:", error);
    if(session.isOpen()){
      session.close(CloseStatus.BAD_DATA.withReason("数据处理失败"));
    }
    sessions.remove(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    Map<String, Object> attributes = session.getAttributes();
    JSONObject jsonObject;
    try {
      jsonObject = JSON.parseObject(payload);
    } catch (RuntimeException e) {
      log.info("skip:" + payload);
      return;
    }
    int limit = jsonObject.getIntValue("limit") > 20 ? 20 : jsonObject.getIntValue("limit");
    attributes.put(jsonObject.getString("symbol"), DepthConfig.builder().symbol(jsonObject.getString("symbol")).limit(limit).build());
  }

  @Override
  public long subscriptions(String symbol) {
    return sessions.stream().filter(v -> v.getAttributes().get(symbol) != null).count();
  }

  @Override
  public void send(String symbol, List<Ask> asks, List<Bid> bids) {
    Assert.notNull(symbol, "交易对不能为空");
    if (asks.size() == 0 && bids.size() == 0) {
      return;
    }
    sessions.stream().filter(v -> v.getAttributes().get(symbol) != null).forEach((session) -> {
      try {
        if(session.isOpen()){
          DepthConfig config = (DepthConfig) session.getAttributes().get(symbol);
          Depth depth = new Depth();
          depth.setSymbol(symbol);
          depth.setAsks(asks.stream().limit(config.getLimit()).collect(Collectors.toList()));
          depth.setBids(bids.stream().limit(config.getLimit()).collect(Collectors.toList()));

          service.submit(() -> {
            try {
              session.sendMessage(new TextMessage(JSON.toJSONString(depth)));
            } catch (Exception e) {
              log.error("发送消息失败:" + session.getId(), e);            }
          });

        } else {
          sessions.remove(session);
        }
      } catch (Exception e) {
        String sessionId = session == null ? "" : session.getId();
        log.error("提交消息失败:" + sessionId, e);
      }
    });
  }

  @Builder
  @Getter
  private static class DepthConfig {
    private String symbol;
    private int limit;
  }

}
