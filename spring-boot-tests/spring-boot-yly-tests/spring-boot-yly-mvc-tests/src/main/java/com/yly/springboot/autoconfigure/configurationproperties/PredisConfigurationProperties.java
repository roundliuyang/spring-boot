package com.yly.springboot.autoconfigure.configurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("mycooltv.predis")
// @Configuration
public class PredisConfigurationProperties {

	/**
	 * predis url
	 */
	public String url = "";

	/**
	 * 是否异步请求predis
	 */
	private boolean async = false;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PredisConfigurationProperties{");
		sb.append("url='").append(url).append('\'');
		sb.append(", async=").append(async);
		sb.append('}');
		return sb.toString();
	}
}
