package com.gop.dataload.depth.service;

import java.util.List; /**
 * Created by Lxa on 2018/3/28.
 *
 * @author lixianan
 */
public interface DepthService {

  long subscriptions(String symbol);

  void send(String symbol, List<Ask> asks, List<Bid> bids);

}
