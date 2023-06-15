package com.yly.springboot.autoconfigure.configurationproperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
// 如果我们在 POJO 中不使用@Configuration ，那么我们需要在 Spring 应用程序主类中添加@EnableConfigurationProperties(ConfigProperties.class)来将属性绑定到 POJO 中
@EnableConfigurationProperties(PredisConfigurationProperties.class)
public class PredisAutoConfiguration {

    @Bean
	@ConditionalOnMissingBean
    @Primary
    public Hello hello() {
        return new Hello("张三三");
    }
}
