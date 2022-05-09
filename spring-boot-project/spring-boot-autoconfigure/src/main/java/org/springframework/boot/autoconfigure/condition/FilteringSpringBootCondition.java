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

package org.springframework.boot.autoconfigure.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Abstract base class for a {@link SpringBootCondition} that also implements
 * {@link AutoConfigurationImportFilter}.
 *
 * @author Phillip Webb
 * 作为具有 AutoConfigurationImportFilter 功能的 SpringBootCondition 的抽象基类
 */
abstract class FilteringSpringBootCondition extends SpringBootCondition
		implements AutoConfigurationImportFilter, BeanFactoryAware, BeanClassLoaderAware {
	// 通过Spring Aware 机制，进行注入
	private BeanFactory beanFactory;
	// 通过Spring Aware 机制，进行注入
	private ClassLoader beanClassLoader;

	// 进行批量的匹配，并返回匹配结果，代码如下
	@Override
	public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
		// 获得 ConditionEvaluationReport 对象
		ConditionEvaluationReport report = ConditionEvaluationReport.find(this.beanFactory);
		// 执行批量的匹配，并返回匹配的结果
		ConditionOutcome[] outcomes = getOutcomes(autoConfigurationClasses, autoConfigurationMetadata);
		// 创建 match 数组
		boolean[] match = new boolean[outcomes.length];
		// 遍历 outcomes 结果数组
		for (int i = 0; i < outcomes.length; i++) {
			// 赋值 match 数组
			match[i] = (outcomes[i] == null || outcomes[i].isMatch());
			// 如果不匹配，打印日志和记录
			if (!match[i] && outcomes[i] != null) {
				logOutcome(autoConfigurationClasses[i], outcomes[i]);
				if (report != null) {
					report.recordConditionEvaluation(autoConfigurationClasses[i], this, outcomes[i]);
				}
			}
		}
		// 返回 match 数组
		return match;
	}

	protected abstract ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses,
			AutoConfigurationMetadata autoConfigurationMetadata);

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	protected final ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	// 通过使用 ClassNameFilter 类，过滤出符合条件的类名的数组。代码如下
	protected final List<String> filter(Collection<String> classNames, ClassNameFilter classNameFilter,
			ClassLoader classLoader) {
		// 如果为空，返回空结果
		if (CollectionUtils.isEmpty(classNames)) {
			return Collections.emptyList();
		}
		// 创建 matches 数组
		List<String> matches = new ArrayList<>(classNames.size());
		// 遍历 classNames 数组，使用 classNameFilter 进行判断是否匹配
		for (String candidate : classNames) {
			if (classNameFilter.matches(candidate, classLoader)) {
				matches.add(candidate);
			}
		}
		// 返回
		return matches;
	}

	/**
	 * Slightly faster variant of {@link ClassUtils#forName(String, ClassLoader)} that
	 * doesn't deal with primitives, arrays or inner types.
	 * @param className the class name to resolve
	 * @param classLoader the class loader to use
	 * @return a resolved class
	 * @throws ClassNotFoundException if the class cannot be found
	 */
	protected static Class<?> resolve(String className, ClassLoader classLoader) throws ClassNotFoundException {
		if (classLoader != null) {
			return classLoader.loadClass(className);
		}
		return Class.forName(className);
	}
	// 内部类，提供判断是否存在的功能。代码如下
	protected enum ClassNameFilter {
		// 指定类存在
		PRESENT {

			@Override
			public boolean matches(String className, ClassLoader classLoader) {
				return isPresent(className, classLoader);
			}

		},
		// 指定类不存在
		MISSING {

			@Override
			public boolean matches(String className, ClassLoader classLoader) {
				return !isPresent(className, classLoader);
			}

		};

		abstract boolean matches(String className, ClassLoader classLoader);
		// 判断是否存在
		static boolean isPresent(String className, ClassLoader classLoader) {
			if (classLoader == null) {
				classLoader = ClassUtils.getDefaultClassLoader();
			}
			try {
				resolve(className, classLoader);
				return true;
			}
			catch (Throwable ex) {
				return false;
			}
		}

	}

}
