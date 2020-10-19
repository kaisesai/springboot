/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.web.servlet.context;

import org.springframework.boot.web.server.WebServer;
import org.springframework.context.SmartLifecycle;

/**
 * {@link SmartLifecycle} to start and stop the {@link WebServer} in a
 * {@link ServletWebServerApplicationContext}.
 *
 * @author Andy Wilkinson
 */
class WebServerStartStopLifecycle implements SmartLifecycle {

	private final ServletWebServerApplicationContext applicationContext;

	private final WebServer webServer;

	private volatile boolean running;

	WebServerStartStopLifecycle(ServletWebServerApplicationContext applicationContext, WebServer webServer) {
		this.applicationContext = applicationContext;
		this.webServer = webServer;
	}

	@Override
	public void start() {
		// 启动 web 服务
		this.webServer.start();
		this.running = true;
		// 发布 servletweb 已经初始化的事件
		this.applicationContext
				.publishEvent(new ServletWebServerInitializedEvent(this.webServer, this.applicationContext));
	}

	@Override
	public void stop() {
		this.webServer.stop();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 1;
	}

}
