package com.yly.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

	@ResponseBody
	@RequestMapping("/hello")
	public String hello() {
		return "world";
	}

}