package com.gop.util.ipaynow;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 表单数据转换器 User: 表单数据型报文转换器 Date: 14-8-13 Time: 下午4:13 To change this template use File | Settings |
 * File Templates.
 */
public class FormDateReportConvertor {

  /**
   * 将数据映射表拼接成表单数据POST样式的字符串 key1=value1&key2=value2
   */
  public static String postFormLinkReport(Map<String, String> dataMap) {
    // if(MapUtils.isEmpty(dataMap)) return "";

    StringBuilder reportBuilder = new StringBuilder();

    List<String> keyList = new ArrayList<String>(dataMap.keySet());
    Collections.sort(keyList);

    for (String key : keyList) {
      if (!StringUtils.isBlank(dataMap.get(key))) {
        reportBuilder.append(key + "=" + dataMap.get(key) + "&");
      }

    }

    reportBuilder.deleteCharAt(reportBuilder.lastIndexOf("&"));

    return reportBuilder.toString();
  }

  /**
   * 将数据映射表拼接成表单数据POST样式的字符串 key1=value1&key2=value2
   * <p>
   * 并且对value进行URLEncoder编码
   */
  public static String postFormLinkReportWithURLEncode(Map<String, String> dataMap,
                                                       String charset) {
    // if(MapUtils.isEmpty(dataMap)) return "";

    StringBuilder reportBuilder = new StringBuilder();

    List<String> keyList = new ArrayList<String>(dataMap.keySet());
    Collections.sort(keyList);

    for (String key : keyList) {
      try {
        reportBuilder.append(key + "=" + URLEncoder.encode(dataMap.get(key), charset) + "&");
      } catch (Exception ex) {
        // ignore to continue
        continue;
      }

    }

    reportBuilder.deleteCharAt(reportBuilder.lastIndexOf("&"));

    return reportBuilder.toString();
  }


  /**
   * 将数据映射表拼接成表单数据POST样式的字符串 key1="value1"&key2="value2"
   */
  public static String postBraceFormLinkReport(Map<String, String> dataMap) {
    // if(MapUtils.isEmpty(dataMap)) return "";

    StringBuilder reportBuilder = new StringBuilder();

    List<String> keyList = new ArrayList<String>(dataMap.keySet());
    Collections.sort(keyList);

    for (String key : keyList) {
      reportBuilder.append(key + "=\"" + dataMap.get(key) + "\"&");
    }

    reportBuilder.deleteCharAt(reportBuilder.lastIndexOf("&"));

    return reportBuilder.toString();
  }

  /**
   * 将数据映射表拼接成表单数据POST样式的字符串 key1="value1"&key2="value2"
   * <p>
   * 并且对value进行URLEncoder编码
   */
  public static String postBraceFormLinkReportWithURLEncode(Map<String, String> dataMap,
                                                            String charset) {
    // if(MapUtils.isEmpty(dataMap)) return "";

    StringBuilder reportBuilder = new StringBuilder();

    List<String> keyList = new ArrayList<String>(dataMap.keySet());
    Collections.sort(keyList);

    for (String key : keyList) {
      try {
        reportBuilder.append(key + "=\"" + URLEncoder.encode(dataMap.get(key), charset) + "\"&");
      } catch (Exception ex) {
        // ignore to continue
        continue;
      }

    }

    reportBuilder.deleteCharAt(reportBuilder.lastIndexOf("&"));

    return reportBuilder.toString();
  }

  /**
   * 表单类型报文解析成数据映射表
   *
   * @param reportCharset --报文本身字符集
   * @param targetCharset --目标字符集
   */
  public static Map<String, String> parseFormDataPatternReportWithDecode(String reportContent,
                                                                         String reportCharset, String targetCharset) {
    if (StringUtils.isBlank(reportContent)) {
      return null;
    }

    String[] domainArray = reportContent.split("&");

    Map<String, String> key_value_map = new HashMap<String, String>();
    for (String domain : domainArray) {
      String[] kvArray = domain.split("=");

      if (kvArray.length == 2) {
        try {
          String decodeString = URLDecoder.decode(kvArray[1], reportCharset);
          String lastInnerValue = new String(decodeString.getBytes(reportCharset), targetCharset);
          key_value_map.put(kvArray[0], lastInnerValue);
        } catch (Exception ex) {
          // ignore
        }

      }
    }

    return key_value_map;
  }

  /**
   * 表单类型报文解析成数据映射表
   */
  public static Map<String, String> parseFormDataPatternReport(String reportContent) {
    if (StringUtils.isBlank(reportContent)) {
      return null;
    }

    String[] domainArray = reportContent.split("&");

    Map<String, String> key_value_map = new HashMap<String, String>();
    for (String domain : domainArray) {
      String[] kvArray = domain.split("=");

      if (kvArray.length == 2) {
        try {
          key_value_map.put(kvArray[0], kvArray[1]);
        } catch (Exception ex) {
          // ignore
        }

      }
    }

    return key_value_map;
  }
}
