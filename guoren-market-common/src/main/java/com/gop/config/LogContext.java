package com.gop.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class LogContext {
    public static String active;

    public static String  init(){
        if(active == null){
            active = System.getProperty("spring.profiles.active");
            if(StringUtils.isEmpty(active)){
                System.out.println("日志启动参数没有设置 -Dspring.profiles.active= {空字符串}/dev/test/prod,将取默认配置{空字符串}。");
            }
        }
       return active;
    }
}
