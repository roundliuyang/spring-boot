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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 *
 * org.springframework.boot.context.properties.EnableConfigurationProperties，
 * 支持将指定的带有 @ConfigurationProperties 注解的类解析出 BeanDefinition（Bean 的前身）并注册，
 * 同时注册一个 BeanPostProcessor 去处理带有 @ConfigurationProperties 注解的 Bean
 *
 * Enable support for {@link ConfigurationProperties @ConfigurationProperties} annotated
 * beans. {@code @ConfigurationProperties} beans can be registered in the standard way
 * (for example using {@link Bean @Bean} methods) or, for convenience, can be specified
 * directly on this annotation.
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/*
	可以看到这个注解也是通过 @Import 注解来驱动某个功能的，是不是发现 @EnableXxx 驱动注解都是以这样的方式来实现的
	那么关于 @Import 注解的实现原理我在很多地方都提到过，这里再提一下，模块驱动注解通常需要结合 @Configuration 注解一起使用，
	因为需要先被当做一个配置类，然后解析到上面有 @Import 注解后则进行处理，对于 @Import 注解的值有三种情况：
		1.该 Class 对象实现了 ImportSelector 接口，调用它的 selectImports(..) 方法获取需要被处理的 Class 对象的名称，也就是可以将它们作为一个 Bean 被 Spring IoC 管理
			该 Class 对象实现了 DeferredImportSelector 接口，和上者的执行时机不同，在所有配置类处理完后再执行，且支持 @Order 排序
		2.该 Class 对象实现了 ImportBeanDefinitionRegistrar 接口，会调用它的 registerBeanDefinitions(..) 方法，自定义地往 BeanDefinitionRegistry 注册中心注册 BeanDefinition（Bean 的前身）
		3.该 Class 对象是一个 @Configuration 配置类，会将这个类作为一个 Bean 被 Spring IoC 管理
	这里的 @EnableConfigurationProperties 注解，通过 @Import 导入 EnableConfigurationPropertiesRegistrar 这个类（实现了 ImportBeanDefinitionRegistrar 接口）
	来实现该功能的，下面会进行分析
 */
@Import(EnableConfigurationPropertiesRegistrar.class)
public @interface EnableConfigurationProperties {

	/**
	 * The bean name of the configuration properties validator.
	 * @since 2.2.0
	 */
	String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

	/**
	 *
	 *  指定的 Class 类对象们
	 * Convenient way to quickly register
	 * {@link ConfigurationProperties @ConfigurationProperties} annotated beans with
	 * Spring. Standard Spring Beans will also be scanned regardless of this value.
	 * @return {@code @ConfigurationProperties} annotated beans to register
	 */
	Class<?>[] value() default {};

}
