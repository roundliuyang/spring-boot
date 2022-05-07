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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.boot.loader.jar.JarFile;

/**
 * Base class for launchers that can start an application with a fully configured
 * classpath backed by one or more {@link Archive}s.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @since 1.0.0
 */
public abstract class Launcher {

	/**
	 * Launch the application. This method is the initial entry point that should be
	 * called by a subclass {@code public static void main(String[] args)} method.
	 * @param args the incoming arguments
	 * @throws Exception if the application fails to launch
	 */
	protected void launch(String[] args) throws Exception {
		// 调用 JarFile 的 #registerUrlProtocolHandler() 方法，注册 Spring Boot 自定义的 URLStreamHandler 实现类，用于 jar 包的加载读取。
		JarFile.registerUrlProtocolHandler();
		// 获取 Archive,并通过Archive 的URL 获得 ClassLoader (这里为 LaunchedURLClassLoader)
		ClassLoader classLoader = createClassLoader(getClassPathArchives());
		// 启动应用程序（创建 MainMethodRunner类并调用其 run 方法）
		// 当 MainMethodRunner 的run 方法被调用，便真正开始启动应用程序了
		launch(args, getMainClass(), classLoader);
	}

	/**
	 * Create a classloader for the specified archives.
	 * @param archives the archives
	 * @return the classloader
	 * @throws Exception if the classloader cannot be created
	 */
	protected ClassLoader createClassLoader(List<Archive> archives) throws Exception {
		List<URL> urls = new ArrayList<>(archives.size());
		for (Archive archive : archives) {
			urls.add(archive.getUrl());
		}
		return createClassLoader(urls.toArray(new URL[0]));
	}

	/**
	 * Create a classloader for the specified URLs.
	 * @param urls the URLs
	 * @return the classloader
	 * @throws Exception if the classloader cannot be created
	 */
	protected ClassLoader createClassLoader(URL[] urls) throws Exception {
		return new LaunchedURLClassLoader(urls, getClass().getClassLoader());
	}

	/**
	 * Launch the application given the archive file and a fully configured classloader.
	 * @param args the incoming arguments
	 * @param mainClass the main class to run
	 * @param classLoader the classloader
	 * @throws Exception if the launch fails
	 *
	 * 该方法负责最终的 Spring Boot 应用真正的启动。
	 */
	protected void launch(String[] args, String mainClass, ClassLoader classLoader) throws Exception {
		// <1> 设置 LaunchedURLClassLoader 作为类加载器,从而保证能够从 jar 加载到相应的类。
		Thread.currentThread().setContextClassLoader(classLoader);
		// <2> 创建 MainMethodRunner 对象，并执行 run 方法，启动 Spring Boot 应用
		createMainMethodRunner(mainClass, args, classLoader).run();
	}

	/**
	 * Create the {@code MainMethodRunner} used to launch the application.
	 * @param mainClass the main class
	 * @param args the incoming arguments
	 * @param classLoader the classloader
	 * @return the main method runner
	 */
	protected MainMethodRunner createMainMethodRunner(String mainClass, String[] args, ClassLoader classLoader) {
		return new MainMethodRunner(mainClass, args);
	}

	/**
	 * Returns the main class that should be launched.
	 * @return the name of the main class
	 * @throws Exception if the main class cannot be obtained
	 */
	protected abstract String getMainClass() throws Exception;

	/**
	 * Returns the archives that will be used to construct the class path.
	 * @return the class path archives
	 * @throws Exception if the class path archives cannot be obtained
	 */
	protected abstract List<Archive> getClassPathArchives() throws Exception;

	protected final Archive createArchive() throws Exception {
		/*
			通过获得当前 Class 类的信息，查找到当前归档文件的路径
			到 File root = new File 之前的部分，这段代码都是在找当前类的所在jar包的绝对路径
			之后下面把这个文件创建出来，并以此创建一个 JarFileArchive 对象。
			而这个 JarFileArchive 是 Archive 的子类，这个 Archive 就可以被 Launcher 启动。
		 */
		ProtectionDomain protectionDomain = getClass().getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
		String path = (location != null) ? location.getSchemeSpecificPart() : null;
		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}
		/*
			root 路径为 jar 包的绝对地址，也就是说创建 JarFileArchive 对象。原因是，Launcher 所在包为 org 下，它的根目录当然是 jar 包的绝对路径哈！
			获得路径之后，创建对应的文件，并检查是否存在
		 */
		File root = new File(path);
		if (!root.exists()) {
			throw new IllegalStateException("Unable to determine code source archive from " + root);
		}
		// 如果是目录，则创建 ExplodedArchive，否则创建 JarFileArchive
		return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
	}

}
