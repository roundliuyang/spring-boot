package com.yly.springboot.controller;

import com.yly.springboot.autoconfigure.PredisConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/demo")
public class DemoController {

	@Resource
	PredisConfigurationProperties predisConfigurationProperties;

	@ResponseBody
	@RequestMapping("/hello")
	public String hello() {
		return "world";
	}
	@ResponseBody
	@RequestMapping("/configurationProperties")
	public String configurationProperties() {
		return predisConfigurationProperties.toString();
	}

}