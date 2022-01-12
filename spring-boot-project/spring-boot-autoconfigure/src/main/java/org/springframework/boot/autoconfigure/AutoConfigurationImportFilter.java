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

package org.springframework.boot.autoconfigure;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * Filter that can be registered in {@code spring.factories} to limit the
 * auto-configuration classes considered. This interface is designed to allow fast removal
 * of auto-configuration classes before their bytecode is even read.
 * <p>
 * An {@link AutoConfigurationImportFilter} may implement any of the following
 * {@link org.springframework.beans.factory.Aware Aware} interfaces, and their respective
 * methods will be called prior to {@link #match}:
 * <ul>
 * <li>{@link EnvironmentAware}</li>
 * <li>{@link BeanFactoryAware}</li>
 * <li>{@link BeanClassLoaderAware}</li>
 * <li>{@link ResourceLoaderAware}</li>
 * </ul>
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
@FunctionalInterface
public interface AutoConfigurationImportFilter {

	/**
	 * Apply the filter to the given auto-configuration class candidates.
	 * @param autoConfigurationClasses the auto-configuration classes being considered.
	 * This array may contain {@code null} elements. Implementations should not change the
	 * values in this array.
	 * @param autoConfigurationMetadata access to the meta-data generated by the
	 * auto-configure annotation processor
	 * @return a boolean array indicating which of the auto-configuration classes should
	 * be imported. The returned array must be the same size as the incoming
	 * {@code autoConfigurationClasses} parameter. Entries containing {@code false} will
	 * not be imported.
	 *
	 *
	 * 将传入的 autoConfigurationClasses 配置雷门，根据 autoConfigurationMetadata 的元数据（主要是注解信息）
	 * 进行匹配，判断是否需要引入，然后返回 boolean 结果。
	 * 并且，boolean[] 结果 和autoConfigurationClasses 配置类是一一对应的关系。假设autoConfigurationClasses[0] 对应的 boolean[0] 为 false ，表示无需引入，反之则需要引入。
	 */
	boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata);

}
