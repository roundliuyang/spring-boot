package com.yly.springboot.autoconfigure.conditionalonproperty;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ConditionalOnProperty 来控制Configuration是否生效
 * 在application.properties配置"mf.assert"，对应的值为true
 */
@Configuration
@ConditionalOnProperty(prefix = "mf", name = "assert", havingValue = "true")
public class AssertConfig {

	@Bean
	public Hello2 hello2() {
		return new Hello2("张三三");
	}

}