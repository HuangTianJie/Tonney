package com.gop.dataload.depth.service;

import java.util.List;
import lombok.Data;

/**
 * Created by Lxa on 2018/3/29.
 *
 * @author lixianan
 */
@Data
public class Depth {
  private String symbol;
  private List<Ask> asks;
  private List<Bid> bids;
}
