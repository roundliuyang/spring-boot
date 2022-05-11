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

package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 *
 * @ConfigurationProperties 注解有两种使用方法，可见 《关与 @EnableConfigurationProperties 注解》 文章。总结来说：
 * 第一种，@Component + @ConfigurationProperties 。
 * @EnableConfigurationProperties（某个 Bean）+ @ConfigurationProperties 注解（另一个普通类）
 * 实际情况下，更多的是使用第一种。第二种方式和第一种原理都是一样的，不过第二种方式会注册一个 BeanPostProcessor
 * 用于处理带有 @ConfigurationProperties 注解的 Spring Bean，同时会将指定的 Class 们解析出 BeanDefinition（Bean 的前身）并注册，这也就是为什么第二种不用标注 @Component 注解
 *
 * 那么第一种方式在哪注册的 BeanPostProcessor 呢？因为 Spring Boot 有一个 ConfigurationPropertiesAutoConfiguration 自动配置类，如下：
 * 很简单，也是通过 @EnableConfigurationProperties 注解注册的这个 BeanPostProcessor 对象
 * Annotation for externalized configuration. Add this to a class definition or a
 * {@code @Bean} method in a {@code @Configuration} class if you want to bind and validate
 * some external Properties (e.g. from a .properties file).
 * <p>
 * Binding is either performed by calling setters on the annotated class or, if
 * {@link ConstructorBinding @ConstructorBinding} is in use, by binding to the constructor
 * parameters.
 * <p>
 * Note that contrary to {@code @Value}, SpEL expressions are not evaluated since property
 * values are externalized.
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see ConfigurationPropertiesScan
 * @see ConstructorBinding
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see EnableConfigurationProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

	/**
	 * 指定的配置项前缀
	 * The prefix of the properties that are valid to bind to this object. Synonym for
	 * {@link #prefix()}. A valid prefix is defined by one or more words separated with
	 * dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix of the properties to bind
	 */
	@AliasFor("prefix")
	String value() default "";

	/**
	 * 指定的配置项前缀
	 * The prefix of the properties that are valid to bind to this object. Synonym for
	 * {@link #value()}. A valid prefix is defined by one or more words separated with
	 * dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix of the properties to bind
	 */
	@AliasFor("value")
	String prefix() default "";

	/**
	 * 是否忽略无效的字段
	 * Flag to indicate that when binding to this object invalid fields should be ignored.
	 * Invalid means invalid according to the binder that is used, and usually this means
	 * fields of the wrong type (or that cannot be coerced into the correct type).
	 * @return the flag value (default false)
	 */
	boolean ignoreInvalidFields() default false;

	/**
	 * 是否忽略不知道的字段
	 * Flag to indicate that when binding to this object unknown fields should be ignored.
	 * An unknown field could be a sign of a mistake in the Properties.
	 * @return the flag value (default true)
	 */
	boolean ignoreUnknownFields() default true;

}
