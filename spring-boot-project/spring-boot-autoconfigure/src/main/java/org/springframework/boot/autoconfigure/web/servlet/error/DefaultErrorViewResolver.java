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

package org.springframework.boot.autoconfigure.web.servlet.error;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * 默认的错误视图解析器
 *
 * Default {@link ErrorViewResolver} implementation that attempts to resolve error views
 * using well known conventions. Will search for templates and static assets under
 * {@code '/error'} using the {@link HttpStatus status code} and the
 * {@link HttpStatus#series() status series}.
 * <p>
 * For example, an {@code HTTP 404} will search (in the specific order):
 * <ul>
 * <li>{@code '/<templates>/error/404.<ext>'}</li>
 * <li>{@code '/<static>/error/404.html'}</li>
 * <li>{@code '/<templates>/error/4xx.<ext>'}</li>
 * <li>{@code '/<static>/error/4xx.html'}</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public class DefaultErrorViewResolver implements ErrorViewResolver, Ordered {

	private static final Map<Series, String> SERIES_VIEWS;

	static {
		Map<Series, String> views = new EnumMap<>(Series.class);
		// 4xx 开头客户端异常
		views.put(Series.CLIENT_ERROR, "4xx");
		// 5xx 开头服务端异常
		views.put(Series.SERVER_ERROR, "5xx");
		SERIES_VIEWS = Collections.unmodifiableMap(views);
	}

	private ApplicationContext applicationContext;

	private final ResourceProperties resourceProperties;

	private final TemplateAvailabilityProviders templateAvailabilityProviders;

	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * Create a new {@link DefaultErrorViewResolver} instance.
	 * @param applicationContext the source application context
	 * @param resourceProperties resource properties
	 */
	public DefaultErrorViewResolver(ApplicationContext applicationContext, ResourceProperties resourceProperties) {
		Assert.notNull(applicationContext, "ApplicationContext must not be null");
		Assert.notNull(resourceProperties, "ResourceProperties must not be null");
		this.applicationContext = applicationContext;
		this.resourceProperties = resourceProperties;
		this.templateAvailabilityProviders = new TemplateAvailabilityProviders(applicationContext);
	}

	DefaultErrorViewResolver(ApplicationContext applicationContext, ResourceProperties resourceProperties,
			TemplateAvailabilityProviders templateAvailabilityProviders) {
		Assert.notNull(applicationContext, "ApplicationContext must not be null");
		Assert.notNull(resourceProperties, "ResourceProperties must not be null");
		this.applicationContext = applicationContext;
		this.resourceProperties = resourceProperties;
		this.templateAvailabilityProviders = templateAvailabilityProviders;
	}

	/**
	 * 解析错误视图
	 *
	 * @param request the source request
	 * @param status  the http status of the error
	 * @param model   the suggested model to be used with the view
	 * @return
	 */
	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		// 解析模型与视图
		ModelAndView modelAndView = resolve(String.valueOf(status.value()), model);
		// 模型与视图不存在，但是状态码符合 4xx 或者 5xx 错误
		if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
			// 继续解析
			modelAndView = resolve(SERIES_VIEWS.get(status.series()), model);
		}
		return modelAndView;
	}

	private ModelAndView resolve(String viewName, Map<String, Object> model) {
		// 拼接视图名称
		String errorViewName = "error/" + viewName;
		// 模板可用提供者，即模板引擎
		TemplateAvailabilityProvider provider = this.templateAvailabilityProviders.getProvider(errorViewName,
				this.applicationContext);
		if (provider != null) {
			return new ModelAndView(errorViewName, model);
		}
		// 解析资源静态 HTML 资源
		return resolveResource(errorViewName, model);
	}

	/**
	 * 解析资源
	 *
	 * @param viewName
	 * @param model
	 * @return
	 */
	private ModelAndView resolveResource(String viewName, Map<String, Object> model) {
		// 遍历静态资源
		for (String location : this.resourceProperties.getStaticLocations()) {
			try {
				// 获取静态资源
				Resource resource = this.applicationContext.getResource(location);
				// static 目录下存在 4xx.html 页面
				resource = resource.createRelative(viewName + ".html");
				if (resource.exists()) {
					return new ModelAndView(new HtmlResourceView(resource), model);
				}
			}
			catch (Exception ex) {
			}
		}
		return null;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * {@link View} backed by an HTML resource.
	 */
	private static class HtmlResourceView implements View {

		private Resource resource;

		HtmlResourceView(Resource resource) {
			this.resource = resource;
		}

		@Override
		public String getContentType() {
			return MediaType.TEXT_HTML_VALUE;
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			response.setContentType(getContentType());
			FileCopyUtils.copy(this.resource.getInputStream(), response.getOutputStream());
		}

	}

}
