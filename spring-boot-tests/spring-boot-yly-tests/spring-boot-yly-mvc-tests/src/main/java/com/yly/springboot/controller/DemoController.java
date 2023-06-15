package com.yly.springboot.controller;

import com.yly.springboot.autoconfigure.configurationproperties.PredisConfigurationProperties;
import com.yly.springboot.autoconfigure.conditionalonproperty.Hello2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/demo")
public class DemoController {

	@Resource
	private PredisConfigurationProperties predisConfigurationProperties;

//	@Resource
//	private Hello2 hello2;

	@ResponseBody
	@RequestMapping("/hello")
	public String hello() {
		return "world";
	}

	/**
	 * 测试 @ConfigurationProperties 注解
	 */
	@ResponseBody
	@RequestMapping("/configurationProperties")
	public String configurationProperties() {
		return predisConfigurationProperties.toString();
	}

//	/**
//	 * 测试 @ConditionalOnProperty 注解
//	 * 如果 mf.assert 值为 false , @Resource 注入失败，项目启动失败
//	 */
//	@ResponseBody
//	@RequestMapping("/conditionalOnProperty")
//	public String conditionalOnProperty() {
//		return hello2.toString();
//	}

}