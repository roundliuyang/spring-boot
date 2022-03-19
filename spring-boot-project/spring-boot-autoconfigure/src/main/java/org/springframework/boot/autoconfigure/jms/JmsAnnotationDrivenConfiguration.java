/*
 * Copyright 2012-2019 the original author or authors.
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

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJndi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerConfigUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Configuration for Spring 4.1 annotation driven JMS.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableJms.class)
class JmsAnnotationDrivenConfiguration {

	private final ObjectProvider<DestinationResolver> destinationResolver;

	private final ObjectProvider<JtaTransactionManager> transactionManager;

	private final ObjectProvider<MessageConverter> messageConverter;

	private final JmsProperties properties;

	JmsAnnotationDrivenConfiguration(ObjectProvider<DestinationResolver> destinationResolver,
			ObjectProvider<JtaTransactionManager> transactionManager, ObjectProvider<MessageConverter> messageConverter,
			JmsProperties properties) {
		// DestinationResolver 用于解决JMS 目标的策略接口，被JmsTemplate 用于将目标名称从简单的字符串解析为实际的 Destination 实现实例
		this.destinationResolver = destinationResolver;
		// JtaTransactionManager  是 PlatformTransactionManager 的实现类，它提供了 Spring 的 JTA 的事务管理，也可用于分布式事务的管理
		this.transactionManager = transactionManager;
		// MessageConverter 是一个策略接口，用于指定 Java 对象和 JMS 消息之间的转换器。
		this.messageConverter = messageConverter;
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	DefaultJmsListenerContainerFactoryConfigurer jmsListenerContainerFactoryConfigurer() {
		DefaultJmsListenerContainerFactoryConfigurer configurer = new DefaultJmsListenerContainerFactoryConfigurer();
		configurer.setDestinationResolver(this.destinationResolver.getIfUnique());
		configurer.setTransactionManager(this.transactionManager.getIfUnique());
		configurer.setMessageConverter(this.messageConverter.getIfUnique());
		configurer.setJmsProperties(this.properties);
		return configurer;
	}

	@Bean
	@ConditionalOnSingleCandidate(ConnectionFactory.class)
	@ConditionalOnMissingBean(name = "jmsListenerContainerFactory")
	DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
			DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		return factory;
	}

	@Configuration(proxyBeanMethods = false)
	@EnableJms
	@ConditionalOnMissingBean(name = JmsListenerConfigUtils.JMS_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
	static class EnableJmsConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnJndi
	static class JndiConfiguration {

		@Bean
		@ConditionalOnMissingBean(DestinationResolver.class)
		JndiDestinationResolver destinationResolver() {
			JndiDestinationResolver resolver = new JndiDestinationResolver();
			resolver.setFallbackToDynamicDestination(true);
			return resolver;
		}

	}

}
