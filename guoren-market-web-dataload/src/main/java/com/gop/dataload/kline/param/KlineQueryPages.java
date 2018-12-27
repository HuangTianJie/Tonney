package com.gop.dataload.kline.param;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Lxa on 2018/1/30.
 *
 * @author lixianan
 */
@Builder
@Getter
@EqualsAndHashCode
public class KlineQueryPages {

  private Pages<KlineRecord> pages;

  @Builder
  @Getter
  @EqualsAndHashCode
  @ToString
  public static class KlineRecord {

    private Long time;

    private String klineType;

    private String symbol;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal maxPrice;

    private BigDecimal lowPrice;

    private BigDecimal amount;

  }

}
