package com.gop.dataload.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Lxa on 2017/4/25.
 *
 * @author lixianan
 */
@Configuration
public class WebServerConfiguration {

  @Bean
  public EmbeddedServletContainerCustomizer containerCustomizer() {
    return container -> {
      if (container instanceof TomcatEmbeddedServletContainerFactory) {
        TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) container;
        //containerFactory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
        containerFactory.addConnectorCustomizers(connector -> {
          connector.setEnableLookups(false);
          Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
          //最大acceptCount
          //protocol.setBacklog(100);
          //设置最大连接数
          //protocol.setMaxConnections(10000);
          //设置最大线程数
          protocol.setMaxThreads(150);
          //protocol.setProcessorCache(200);
          protocol.setConnectionTimeout(20000);
          protocol.setKeepAliveTimeout(20000);
          protocol.setMaxKeepAliveRequests(100);
        });
      }
    };
  }
}
