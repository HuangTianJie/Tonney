package com.gop.dataload.kline.param;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Lxa on 2017/5/3.
 *
 * @author lixianan
 */
@Getter
@EqualsAndHashCode
@ToString
public class Pages<T> {
  private static final long serialVersionUID = 1L;
  //当前页
  private final int pageNum;
  //每页的数量
  private final int pageSize;
  //当前页的数量
  private int size;
  //总记录数
  private final long total;
  //总页数
  private final long pages;
  //limit起始索引
  private final int startIndex;
  //结果集
  private List<T> list;


  public Pages(int pageNum, int pageSize, long total) {
    this(pageNum, pageSize, total, new ArrayList<T>());
  }

  public Pages(int pageNum, int pageSize, long total, List<T> list) {
    if (pageNum < 1) {
      throw new IllegalArgumentException("页数非法" + pageNum);
    }
    if (pageSize < 1) {
      throw new IllegalArgumentException("每页展示数量非法" + pageSize);
    }
    this.pageNum = pageNum;
    this.pageSize = pageSize;
    this.total = total;
    this.pages = (total - 1) / pageSize + 1;
    this.startIndex = (pageNum - 1) * pageSize;
    this.size = list.size();
    this.list = list;
  }

  public void setList(List<T> list) {
    this.size = list.size();
    this.list = list;
  }
}