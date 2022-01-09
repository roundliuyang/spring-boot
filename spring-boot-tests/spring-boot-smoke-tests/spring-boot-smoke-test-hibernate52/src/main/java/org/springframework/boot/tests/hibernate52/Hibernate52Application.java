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

package org.springframework.boot.tests.hibernate52;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication 注解，它在 spring-boot-autoconfigure 模块中。所以，我们使用 Spring Boot 项目时，
 * 如果不想使用自动配置功能，就不用引入它。当然，我们貌似不太会存在这样的需求，是吧~
 */
@SpringBootApplication
public class Hibernate52Application {

	public static void main(String[] args) {
		SpringApplication.run(Hibernate52Application.class, args);
	}

}
