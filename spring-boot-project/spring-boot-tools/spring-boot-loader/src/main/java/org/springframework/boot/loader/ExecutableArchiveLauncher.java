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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import org.springframework.boot.loader.archive.Archive;

/**
 * Base class for executable archive {@link Launcher}s.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.0.0
 */
public abstract class ExecutableArchiveLauncher extends Launcher {

	private final Archive archive;

	public ExecutableArchiveLauncher() {
		try {
			this.archive = createArchive();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected ExecutableArchiveLauncher(Archive archive) {
		this.archive = archive;
	}

	protected final Archive getArchive() {
		return this.archive;
	}

	/*
		从 jar 包的 MANIFEST.MF 文件的 Start-Class 配置项，，获得我们设置的 Spring Boot 的主启动类。
	 */
	@Override
	protected String getMainClass() throws Exception {
		// 获得启动的类的全名,如 cn.iocoder.springboot.lab39.skywalkingdemo.Application
		Manifest manifest = this.archive.getManifest();
		String mainClass = null;
		if (manifest != null) {
			mainClass = manifest.getMainAttributes().getValue("Start-Class");
		}
		if (mainClass == null) {
			throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
		}
		return mainClass;
	}

	@Override
	protected List<Archive> getClassPathArchives() throws Exception {
		/*
			获得所有 Archive
			this::isNestedArchive 代码段，创建了 EntryFilter 匿名实现类，用于过滤 jar 包不需要的目录。
			archive 对象要执行 getNestedArchives 时，会传入一个 EntryFilter，以此来获取一组被嵌套的 Archive 。
			而这个 EntryFilter 的工作机制就是上面的 isNestedArchive 方法，在 JarLauncher 中也有定义：

					protected boolean isNestedArchive(Archive.Entry entry) {
   						 if (entry.isDirectory()) {
       						 return entry.getName().equals(BOOT_INF_CLASSES);
   				   		 }
   					 	return entry.getName().startsWith(BOOT_INF_LIB);
					}

			 现在是不是对 Archive 稍微有点感觉落？继续附加如下代码，打印 JarFileArchive 的 #getNestedArchives(EntryFilter filter) 方法的执行结果。
			 从执行结果可以看出，BOOT-INF/classes/ 目录被归类为一个 Archive 对象，而 BOOT-INF/lib/ 目录下的每个内嵌 jar 包都对应一个 Archive 对象。
		 */
		List<Archive> archives = new ArrayList<>(this.archive.getNestedArchives(this::isNestedArchive));
		// 后续处理
		postProcessClassPathArchives(archives);
		return archives;
	}

	/**
	 * Determine if the specified {@link JarEntry} is a nested item that should be added
	 * to the classpath. The method is called once for each entry.
	 * @param entry the jar entry
	 * @return {@code true} if the entry is a nested item (jar or folder)
	 */
	protected abstract boolean isNestedArchive(Archive.Entry entry);

	/**
	 * Called to post-process archive entries before they are used. Implementations can
	 * add and remove entries.
	 * @param archives the archives
	 * @throws Exception if the post processing fails
	 */
	protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
	}

}
