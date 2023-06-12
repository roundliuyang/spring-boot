package com.yly.springboot.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class PredisAutoConfiguration {

    @Bean
    @Primary
    public Hello jobExecutor() {
        return new Hello("张三三");
    }
}
