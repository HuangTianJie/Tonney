package com.gte;

import com.gop.web.base.auth.AuthContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
 */
@Configuration
@EnableSwagger2
public class Swagger2 {

    private static final String VERSION = "1.5.4.1";

    @Value("${swagger.enable}")
    private boolean enableSwagger;

    @Bean
    public Docket createRestApi() {
        //添加head参数start
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        tokenPar.name("authorization").description("令牌")
                .modelRef(new ModelRef("string")).parameterType("header")
                .defaultValue("token=eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvdXJkYXhXZWIiLCJzdWIiOiIxOCIsImF1ZCI6IndlYiIsImlhdCI6MTU0MjE1OTk0MywiZXhwIjoxNTQyNTE5OTQzfQ.PnLQejeK2Y_PYW6OSeQjRihHaK8i9Gh1_gK5j3UGM3yg5RjR0QeKzeeO8QBTluk6kC2xzHfP2tLNcyOPA_rOxw")
                .required(false).build();
        pars.add(tokenPar.build());
        ParameterBuilder language = new ParameterBuilder();
        language.name("accept-language").description("语言").modelRef(new ModelRef("string")).parameterType("header").required(true).defaultValue("zh-CN").build();
        pars.add(language.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .enable(enableSwagger)
                .apiInfo(apiInfo())
                .ignoredParameterTypes(AuthContext.class)
                .select()
                //为当前包路径
                .apis(RequestHandlerSelectors.basePackage("com"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars);
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("Spring Boot 测试使用 Swagger2 构建RESTful API -- exchange_agent_manager")
                //创建人
                .contact(new Contact("King", "#", ""))
                //版本号
                .version(VERSION)
                //描述
                .description("API 描述")
                .build();
    }


}