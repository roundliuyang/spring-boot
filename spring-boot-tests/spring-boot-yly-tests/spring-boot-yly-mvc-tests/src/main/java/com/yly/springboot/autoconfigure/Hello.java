package com.yly.springboot.autoconfigure;

public class Hello {
	public String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Hello(String name) {
		this.name = name;
	}
}
