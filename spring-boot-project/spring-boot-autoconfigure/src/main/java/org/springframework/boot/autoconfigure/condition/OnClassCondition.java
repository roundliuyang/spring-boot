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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * {@link Condition} and {@link AutoConfigurationImportFilter} that checks for the
 * presence or absence of specific classes.
 *
 * @author Phillip Webb
 * @see ConditionalOnClass
 * @see ConditionalOnMissingClass
 */
// 给 @ConditionalOnClass、@ConditionalOnMissingClass 使用的 Condition 实现类。
@Order(Ordered.HIGHEST_PRECEDENCE)
class OnClassCondition extends FilteringSpringBootCondition {

	// 来自 FilteringSpringBootCondition 抽象类
	@Override
	protected final ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		// Split the work and perform half in a background thread if more than one
		// processor is available. Using a single additional thread seems to offer the
		// best performance. More threads make things worse.
		// 在后台线程中将工作一分为二。原因是：
		// 使用单一附加线程，似乎提供了最好的功能
		// 多个线程事情似乎变得更糟
		// 考虑到配置类（Configuration）配置的 @ConditionalOnClass、@ConditionalOnMissingClass 注解中的类可能比较多，所以采用多线程提升效率。
		// 但是经过测试，分成两个线程，效率是最好的，所以这里才出现了 autoConfigurationClasses.length / 2 代码。
		if (Runtime.getRuntime().availableProcessors() > 1) {
			return resolveOutcomesThreaded(autoConfigurationClasses, autoConfigurationMetadata);
		}
		else {
			OutcomesResolver outcomesResolver = new StandardOutcomesResolver(autoConfigurationClasses, 0,
					autoConfigurationClasses.length, autoConfigurationMetadata, getBeanClassLoader());
			return outcomesResolver.resolveOutcomes();
		}
	}

	private ConditionOutcome[] resolveOutcomesThreaded(String[] autoConfigurationClasses,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		int split = autoConfigurationClasses.length / 2;
		// 将前一半，创建一个 OutcomesResolver 对象（新线程）
		OutcomesResolver firstHalfResolver = createOutcomesResolver(autoConfigurationClasses, 0, split,
				autoConfigurationMetadata);
		// 将后一半，创建一个 OutcomesResolver 对象
		OutcomesResolver secondHalfResolver = new StandardOutcomesResolver(autoConfigurationClasses, split,
				autoConfigurationClasses.length, autoConfigurationMetadata, getBeanClassLoader());
		// 执行解析（匹配），调用后一半的 StandardOutcomesResolver#resolveOutcomes() 方法，执行解析（匹配）。
		ConditionOutcome[] secondHalf = secondHalfResolver.resolveOutcomes();
		// 调用前一半的 ThreadedOutcomesResolver#resolveOutcomes() 方法，执行解析（匹配）。在 ThreadedOutcomesResolver 的实现里，
		// 会使用 Thread#join() 方法，保证新起的线程，能完成它的任务。这也是为什么，ThreadedOutcomesResolver 后执行的原因。
		ConditionOutcome[] firstHalf = firstHalfResolver.resolveOutcomes();
		// 创建 outcomes 结果数组，然后合并结果，最后返回。
		ConditionOutcome[] outcomes = new ConditionOutcome[autoConfigurationClasses.length];
		System.arraycopy(firstHalf, 0, outcomes, 0, firstHalf.length);
		System.arraycopy(secondHalf, 0, outcomes, split, secondHalf.length);
		return outcomes;
	}

	private OutcomesResolver createOutcomesResolver(String[] autoConfigurationClasses, int start, int end,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		// 首先创建 StandardOutcomesResolver 对象
		OutcomesResolver outcomesResolver = new StandardOutcomesResolver(autoConfigurationClasses, start, end,
				autoConfigurationMetadata, getBeanClassLoader());
		// 创建了 ThreadedOutcomesResolver 对象，将 outcomesResolver 包装在其中。注意噢，下文我们会看到，ThreadedOutcomesResolver 是启动了一个新线程，执行 StandardOutcomesResolver 的逻辑。
		try {
			return new ThreadedOutcomesResolver(outcomesResolver);
		}
		catch (AccessControlException ex) {
			return outcomesResolver;
		}
	}

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ClassLoader classLoader = context.getClassLoader();
		ConditionMessage matchMessage = ConditionMessage.empty();
		List<String> onClasses = getCandidates(metadata, ConditionalOnClass.class);
		if (onClasses != null) {
			List<String> missing = filter(onClasses, ClassNameFilter.MISSING, classLoader);
			if (!missing.isEmpty()) {
				return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnClass.class)
						.didNotFind("required class", "required classes").items(Style.QUOTE, missing));
			}
			matchMessage = matchMessage.andCondition(ConditionalOnClass.class)
					.found("required class", "required classes")
					.items(Style.QUOTE, filter(onClasses, ClassNameFilter.PRESENT, classLoader));
		}
		List<String> onMissingClasses = getCandidates(metadata, ConditionalOnMissingClass.class);
		if (onMissingClasses != null) {
			List<String> present = filter(onMissingClasses, ClassNameFilter.PRESENT, classLoader);
			if (!present.isEmpty()) {
				return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnMissingClass.class)
						.found("unwanted class", "unwanted classes").items(Style.QUOTE, present));
			}
			matchMessage = matchMessage.andCondition(ConditionalOnMissingClass.class)
					.didNotFind("unwanted class", "unwanted classes")
					.items(Style.QUOTE, filter(onMissingClasses, ClassNameFilter.MISSING, classLoader));
		}
		return ConditionOutcome.match(matchMessage);
	}

	private List<String> getCandidates(AnnotatedTypeMetadata metadata, Class<?> annotationType) {
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(annotationType.getName(), true);
		if (attributes == null) {
			return null;
		}
		List<String> candidates = new ArrayList<>();
		addAll(candidates, attributes.get("value"));
		addAll(candidates, attributes.get("name"));
		return candidates;
	}

	private void addAll(List<String> list, List<Object> itemsToAdd) {
		if (itemsToAdd != null) {
			for (Object item : itemsToAdd) {
				Collections.addAll(list, (String[]) item);
			}
		}
	}

	/**
	 * 内部接口，结果解析器接口
	 * 它的实现类有：
	 * ThreadedOutcomesResolver
	 * StandardOutcomesResolver
	 */
	private interface OutcomesResolver {

		ConditionOutcome[] resolveOutcomes();

	}

	/**
	 * 是 OnClassCondition 的内部类，实现 OutcomesResolver 接口，开启线程，执行OutComesResolver 的逻辑
	 */
	private static final class ThreadedOutcomesResolver implements OutcomesResolver {

		// 新起的线程
		private final Thread thread;
		/**
		 * 条件匹配的结果
		 */
		private volatile ConditionOutcome[] outcomes;

		private ThreadedOutcomesResolver(OutcomesResolver outcomesResolver) {
			// 创建线程
			this.thread = new Thread(() -> this.outcomes = outcomesResolver.resolveOutcomes());
			// 启动线程
			this.thread.start();
		}

		@Override
		public ConditionOutcome[] resolveOutcomes() {
			// 等待线程执行结束
			try {
				this.thread.join();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			// 返回结果
			return this.outcomes;
		}

	}

	/**
	 * 内部类，实现OutcomesResolver 接口，标准的 StandardOutcomesResolver 实现类
	 */
	private final class StandardOutcomesResolver implements OutcomesResolver {
		// 所有配置类的数组
		private final String[] autoConfigurationClasses;
		// 匹配的 {@link #autoConfigurationClasses} 开始位置
		private final int start;
		// 匹配的 {@link #autoConfigurationClasses} 结束位置
		private final int end;

		private final AutoConfigurationMetadata autoConfigurationMetadata;

		private final ClassLoader beanClassLoader;

		private StandardOutcomesResolver(String[] autoConfigurationClasses, int start, int end,
				AutoConfigurationMetadata autoConfigurationMetadata, ClassLoader beanClassLoader) {
			this.autoConfigurationClasses = autoConfigurationClasses;
			this.start = start;
			this.end = end;
			this.autoConfigurationMetadata = autoConfigurationMetadata;
			this.beanClassLoader = beanClassLoader;
		}
		// 执行批量匹配，并返回结果
		@Override
		public ConditionOutcome[] resolveOutcomes() {
			return getOutcomes(this.autoConfigurationClasses, this.start, this.end, this.autoConfigurationMetadata);
		}

		private ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses, int start, int end,
				AutoConfigurationMetadata autoConfigurationMetadata) {
			// 创建ConditionOutcome 结构数组
			ConditionOutcome[] outcomes = new ConditionOutcome[end - start];
			// 遍历
			for (int i = start; i < end; i++) {
				String autoConfigurationClass = autoConfigurationClasses[i];
				if (autoConfigurationClass != null) {
					//  获得指定自动配置类的 @ConditionalOnClass 注解的要求类
					// 加载META-INF/spring-autoconfigure-metadata.properties中的配置
					String candidates = autoConfigurationMetadata.get(autoConfigurationClass, "ConditionalOnClass");
					// 执行匹配
					if (candidates != null) {
						outcomes[i - start] = getOutcome(candidates);
					}
				}
			}
			return outcomes;
		}
		// 执行匹配
		private ConditionOutcome getOutcome(String candidates) {
			// 如果没有，说明只有一个
			try {
				if (!candidates.contains(",")) {
					return getOutcome(candidates, this.beanClassLoader);
				}
				// 如果有，说明有多个，逐个匹配
				for (String candidate : StringUtils.commaDelimitedListToStringArray(candidates)) {
					ConditionOutcome outcome = getOutcome(candidate, this.beanClassLoader);
					// 如果存在不匹配，则返回该结果
					if (outcome != null) {
						return outcome;
					}
				}
			}
			catch (Exception ex) {
				// We'll get another chance later
			}
			return null;
		}

		private ConditionOutcome getOutcome(String className, ClassLoader classLoader) {
			if (ClassNameFilter.MISSING.matches(className, classLoader)) {
				return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnClass.class)
						.didNotFind("required class").items(Style.QUOTE, className));
			}
			return null;
		}

	}

}
