/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jms;

import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJndi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration.JndiOrPropertyCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for JMS provided from JNDI.
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnMissingBean(ConnectionFactory.class)
@Conditional(JndiOrPropertyCondition.class)
@EnableConfigurationProperties(JmsProperties.class)
public class JndiConnectionFactoryAutoConfiguration {

	// Keep these in sync with the condition below
	// 该地址数组，与下面的条件保持一致
	private static final String[] JNDI_LOCATIONS = { "java:/JmsXA", "java:/XAConnectionFactory" };

	@Bean
	public ConnectionFactory jmsConnectionFactory(JmsProperties properties) throws NamingException {
		// 如果配置文件中配置了JNDI 名称，则通过指定的 JNDI 名称获取 ConnectionFactory
		JndiLocatorDelegate jndiLocatorDelegate = JndiLocatorDelegate.createDefaultResourceRefLocator();
		if (StringUtils.hasLength(properties.getJndiName())) {
			return jndiLocatorDelegate.lookup(properties.getJndiName(), ConnectionFactory.class);
		}
		// 如果配置文件中未配置 JNDI 名称，则使用默认的名称进行查找
		return findJndiConnectionFactory(jndiLocatorDelegate);
	}

	private ConnectionFactory findJndiConnectionFactory(JndiLocatorDelegate jndiLocatorDelegate) {
		// 便利默认值进行查找
		for (String name : JNDI_LOCATIONS) {
			try {
				return jndiLocatorDelegate.lookup(name, ConnectionFactory.class);
			}
			catch (NamingException ex) {
				// Swallow and continue
				// 吞没异常并继续
			}
		}
		throw new IllegalStateException(
				"Unable to find ConnectionFactory in JNDI locations " + Arrays.asList(JNDI_LOCATIONS));
	}

	/**
	 * Condition for JNDI name or a specific property.
	 */
	static class JndiOrPropertyCondition extends AnyNestedCondition {

		JndiOrPropertyCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnJndi({ "java:/JmsXA", "java:/XAConnectionFactory" })
		static class Jndi {

		}

		@ConditionalOnProperty(prefix = "spring.jms", name = "jndi-name")
		static class Property {

		}

	}

}
