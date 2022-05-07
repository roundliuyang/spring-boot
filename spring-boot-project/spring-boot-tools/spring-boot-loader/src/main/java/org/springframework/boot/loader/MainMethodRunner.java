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

package org.springframework.boot.loader;

import java.lang.reflect.Method;

/**
 * Utility class that is used by {@link Launcher}s to call a main method. The class
 * containing the main method is loaded using the thread context class loader.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.0.0
 */
public class MainMethodRunner {

	private final String mainClassName;

	private final String[] args;

	/**
	 * Create a new {@link MainMethodRunner} instance.
	 * @param mainClass the main class
	 * @param args incoming arguments
	 */
	public MainMethodRunner(String mainClass, String[] args) {
		this.mainClassName = mainClass;
		this.args = (args != null) ? args.clone() : null;
	}

	/**
	 * 上述代码属性 mainClass 参数便是在 Manifest.MF 文件中我们自定义的Spring Boot 的入口类，即Start-class 属性值。
	 * 在 MainMethodRunner 的 run 方法中，通过反射获得入口类的 main 方法并调用。
	 * 至此，SpringBoot 入口类的 main 方法正式执行，所有应用程序类文件均可通过/BOOT-INF/class 加载
	 * 所有依赖的第三方jar 均可通过 /BOOT-INF/lib 加载。
	 * @throws Exception
	 */
	public void run() throws Exception {
		//  <1> 加载 Spring Boot: 通过 LaunchedURLClassLoader 类加载器，加载到我们设置的 Spring Boot 的主启动类。
		Class<?> mainClass = Thread.currentThread().getContextClassLoader().loadClass(this.mainClassName);
		// <2> 反射调用 main 方法,启动 Spring Boot 应用。这里也告诉了我们答案，为什么我们通过编写一个带有 #main(String[] args) 方法的类，就能够启动 Spring Boot 应用。
		Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
		mainMethod.invoke(null, new Object[] { this.args });
	}

}
