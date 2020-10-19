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

package org.springframework.boot.autoconfigure.web.servlet;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.Ordered;

/**
 * web 服务工厂自定义器
 *
 * {@link WebServerFactoryCustomizer} to apply {@link ServerProperties} to servlet web
 * servers.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Olivier Lamy
 * @author Yunkun Huang
 * @since 2.0.0
 */
public class ServletWebServerFactoryCustomizer
		implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>, Ordered {

	private final ServerProperties serverProperties;

	public ServletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		//设置端口
		map.from(this.serverProperties::getPort).to(factory::setPort);
		// 配置地址
		map.from(this.serverProperties::getAddress).to(factory::setAddress);
		// 配置上下文路径
		map.from(this.serverProperties.getServlet()::getContextPath).to(factory::setContextPath);
		// 配置程序展示名称
		map.from(this.serverProperties.getServlet()::getApplicationDisplayName).to(factory::setDisplayName);
		// 配置是否注册默认的 servlet
		map.from(this.serverProperties.getServlet()::isRegisterDefaultServlet).to(factory::setRegisterDefaultServlet);
		// 配置 session
		map.from(this.serverProperties.getServlet()::getSession).to(factory::setSession);
		// 配置 ssl
		map.from(this.serverProperties::getSsl).to(factory::setSsl);
		// 配置 jsp
		map.from(this.serverProperties.getServlet()::getJsp).to(factory::setJsp);
		// 配置压缩
		map.from(this.serverProperties::getCompression).to(factory::setCompression);
		// 配置 http2
		map.from(this.serverProperties::getHttp2).to(factory::setHttp2);
		// 配置 server 头
		map.from(this.serverProperties::getServerHeader).to(factory::setServerHeader);
		// 配置初始化上下文参数
		map.from(this.serverProperties.getServlet()::getContextParameters).to(factory::setInitParameters);
		// 配置服务停止设置
		map.from(this.serverProperties.getShutdown()).to(factory::setShutdown);
	}

}
