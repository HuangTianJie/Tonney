package com.gop.dataload.depth.service;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Lxa on 2018/3/29.
 *
 * @author lixianan
 */
@Builder
@Getter
public class Ask {
  private String price;
  private String amount;
}
