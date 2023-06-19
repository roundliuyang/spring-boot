package com.yly.springboot.source.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class MyApplicationContextInitializer implements ApplicationContextInitializer {
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		System.out.println("我要 initializer 了哦，请注意我的顺序----------------------------------------------------------");
	}
}
