package com.gop.dataload.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Lxa on 2018/2/1.
 *
 * @author lixianan
 */
@Configuration
public class MongoDbSettings {

  @Bean
  public MongoClientOptions mongoOptions() {
    return MongoClientOptions.builder().connectionsPerHost(150).minConnectionsPerHost(0).socketTimeout(20000).build();
  }

}
