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

package org.springframework.boot.web.servlet;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * A collection {@link ServletContextInitializer}s obtained from a
 * {@link ListableBeanFactory}. Includes all {@link ServletContextInitializer} beans and
 * also adapts {@link Servlet}, {@link Filter} and certain {@link EventListener} beans.
 * <p>
 * Items are sorted so that adapted beans are top ({@link Servlet}, {@link Filter} then
 * {@link EventListener}) and direct {@link ServletContextInitializer} beans are at the
 * end. Further sorting is applied within these groups using the
 * {@link AnnotationAwareOrderComparator}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Brian Clozel
 * @since 1.4.0
 */
public class ServletContextInitializerBeans extends AbstractCollection<ServletContextInitializer> {

	private static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private static final Log logger = LogFactory.getLog(ServletContextInitializerBeans.class);

	/**
	 * Seen bean instances or bean names.
	 */
	private final Set<Object> seen = new HashSet<>();

	private final MultiValueMap<Class<?>, ServletContextInitializer> initializers;

	private final List<Class<? extends ServletContextInitializer>> initializerTypes;

	private List<ServletContextInitializer> sortedList;

	@SafeVarargs
	@SuppressWarnings("varargs")
	public ServletContextInitializerBeans(ListableBeanFactory beanFactory,
			Class<? extends ServletContextInitializer>... initializerTypes) {
		// servletContext 初始化器
		this.initializers = new LinkedMultiValueMap<>();
		this.initializerTypes = (initializerTypes.length != 0) ? Arrays.asList(initializerTypes)
				: Collections.singletonList(ServletContextInitializer.class);
		// 添加初始化器
		addServletContextInitializerBeans(beanFactory);
		// 添加适配器 bean
		addAdaptableBeans(beanFactory);
		// 对初始化器进行排序
		List<ServletContextInitializer> sortedInitializers = this.initializers.values().stream()
				.flatMap((value) -> value.stream().sorted(AnnotationAwareOrderComparator.INSTANCE))
				.collect(Collectors.toList());
		this.sortedList = Collections.unmodifiableList(sortedInitializers);
		// 打印映射日志
		logMappings(this.initializers);
	}

	private void addServletContextInitializerBeans(ListableBeanFactory beanFactory) {
		// 添加把初始化器类型
		for (Class<? extends ServletContextInitializer> initializerType : this.initializerTypes) {
			for (Entry<String, ? extends ServletContextInitializer> initializerBean : getOrderedBeansOfType(beanFactory,
					initializerType)) {
				// 添加 servletContext 初始化器
				addServletContextInitializerBean(initializerBean.getKey(), initializerBean.getValue(), beanFactory);
			}
		}
	}

	private void addServletContextInitializerBean(String beanName, ServletContextInitializer initializer,
			ListableBeanFactory beanFactory) {
		if (initializer instanceof ServletRegistrationBean) {
			Servlet source = ((ServletRegistrationBean<?>) initializer).getServlet();
			// 添加 servlet 类
			addServletContextInitializerBean(Servlet.class, beanName, initializer, beanFactory, source);
		}
		else if (initializer instanceof FilterRegistrationBean) {
			Filter source = ((FilterRegistrationBean<?>) initializer).getFilter();
			// 添加 filter 类
			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
		}
		else if (initializer instanceof DelegatingFilterProxyRegistrationBean) {
			String source = ((DelegatingFilterProxyRegistrationBean) initializer).getTargetBeanName();
			// 添加委派的过滤器代理注册 bean
			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
		}
		else if (initializer instanceof ServletListenerRegistrationBean) {
			EventListener source = ((ServletListenerRegistrationBean<?>) initializer).getListener();
			// 添加 servlet 监听器注册 bean
			addServletContextInitializerBean(EventListener.class, beanName, initializer, beanFactory, source);
		}
		else {
			// 添加 servletContext 初始化器
			addServletContextInitializerBean(ServletContextInitializer.class, beanName, initializer, beanFactory,
					initializer);
		}
	}

	private void addServletContextInitializerBean(Class<?> type, String beanName, ServletContextInitializer initializer,
			ListableBeanFactory beanFactory, Object source) {
		this.initializers.add(type, initializer);
		if (source != null) {
			// Mark the underlying source as seen in case it wraps an existing bean
			this.seen.add(source);
		}
		if (logger.isTraceEnabled()) {
			String resourceDescription = getResourceDescription(beanName, beanFactory);
			int order = getOrder(initializer);
			logger.trace("Added existing " + type.getSimpleName() + " initializer bean '" + beanName + "'; order="
					+ order + ", resource=" + resourceDescription);
		}
	}

	private String getResourceDescription(String beanName, ListableBeanFactory beanFactory) {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			return registry.getBeanDefinition(beanName).getResourceDescription();
		}
		return "unknown";
	}

	@SuppressWarnings("unchecked")
	protected void addAdaptableBeans(ListableBeanFactory beanFactory) {
		// 获取多部分配置
		MultipartConfigElement multipartConfig = getMultipartConfig(beanFactory);
		// 添加 servlet 注册器 bean 适配器
		addAsRegistrationBean(beanFactory, Servlet.class, new ServletRegistrationBeanAdapter(multipartConfig));
		// 添加 filter 注册 bean 适配器
		addAsRegistrationBean(beanFactory, Filter.class, new FilterRegistrationBeanAdapter());
		for (Class<?> listenerType : ServletListenerRegistrationBean.getSupportedTypes()) {
			// 添加事件监听器适配器
			addAsRegistrationBean(beanFactory, EventListener.class, (Class<EventListener>) listenerType,
					new ServletListenerRegistrationBeanAdapter());
		}
	}

	private MultipartConfigElement getMultipartConfig(ListableBeanFactory beanFactory) {
		List<Entry<String, MultipartConfigElement>> beans = getOrderedBeansOfType(beanFactory,
				MultipartConfigElement.class);
		return beans.isEmpty() ? null : beans.get(0).getValue();
	}

	protected <T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type,
			RegistrationBeanAdapter<T> adapter) {
		addAsRegistrationBean(beanFactory, type, type, adapter);
	}

	private <T, B extends T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type,
			Class<B> beanType, RegistrationBeanAdapter<T> adapter) {
		List<Map.Entry<String, B>> entries = getOrderedBeansOfType(beanFactory, beanType, this.seen);
		for (Entry<String, B> entry : entries) {
			String beanName = entry.getKey();
			B bean = entry.getValue();
			if (this.seen.add(bean)) {
				// One that we haven't already seen
				RegistrationBean registration = adapter.createRegistrationBean(beanName, bean, entries.size());
				int order = getOrder(bean);
				registration.setOrder(order);
				this.initializers.add(type, registration);
				if (logger.isTraceEnabled()) {
					logger.trace("Created " + type.getSimpleName() + " initializer for bean '" + beanName + "'; order="
							+ order + ", resource=" + getResourceDescription(beanName, beanFactory));
				}
			}
		}
	}

	private int getOrder(Object value) {
		return new AnnotationAwareOrderComparator() {
			@Override
			public int getOrder(Object obj) {
				return super.getOrder(obj);
			}
		}.getOrder(value);
	}

	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type) {
		return getOrderedBeansOfType(beanFactory, type, Collections.emptySet());
	}

	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type,
			Set<?> excludes) {
		String[] names = beanFactory.getBeanNamesForType(type, true, false);
		Map<String, T> map = new LinkedHashMap<>();
		for (String name : names) {
			if (!excludes.contains(name) && !ScopedProxyUtils.isScopedTarget(name)) {
				T bean = beanFactory.getBean(name, type);
				if (!excludes.contains(bean)) {
					map.put(name, bean);
				}
			}
		}
		List<Entry<String, T>> beans = new ArrayList<>(map.entrySet());
		beans.sort((o1, o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getValue(), o2.getValue()));
		return beans;
	}

	private void logMappings(MultiValueMap<Class<?>, ServletContextInitializer> initializers) {
		if (logger.isDebugEnabled()) {
			//打印 filter 的映射
			logMappings("filters", initializers, Filter.class, FilterRegistrationBean.class);
			//打印 servlet 的映射
			logMappings("servlets", initializers, Servlet.class, ServletRegistrationBean.class);
		}
	}

	private void logMappings(String name, MultiValueMap<Class<?>, ServletContextInitializer> initializers,
			Class<?> type, Class<? extends RegistrationBean> registrationType) {
		// 从初始器中获取需要查询的类型
		List<ServletContextInitializer> registrations = new ArrayList<>();
		registrations.addAll(initializers.getOrDefault(registrationType, Collections.emptyList()));
		registrations.addAll(initializers.getOrDefault(type, Collections.emptyList()));
		String info = registrations.stream().map(Object::toString).collect(Collectors.joining(", "));
		// 打印收集的类信息
		logger.debug("Mapping " + name + ": " + info);
	}

	@Override
	public Iterator<ServletContextInitializer> iterator() {
		return this.sortedList.iterator();
	}

	@Override
	public int size() {
		return this.sortedList.size();
	}

	/**
	 * Adapter to convert a given Bean type into a {@link RegistrationBean} (and hence a
	 * {@link ServletContextInitializer}).
	 *
	 * @param <T> the type of the Bean to adapt
	 */
	@FunctionalInterface
	protected interface RegistrationBeanAdapter<T> {

		RegistrationBean createRegistrationBean(String name, T source, int totalNumberOfSourceBeans);

	}

	/**
	 * {@link RegistrationBeanAdapter} for {@link Servlet} beans.
	 */
	private static class ServletRegistrationBeanAdapter implements RegistrationBeanAdapter<Servlet> {

		private final MultipartConfigElement multipartConfig;

		ServletRegistrationBeanAdapter(MultipartConfigElement multipartConfig) {
			this.multipartConfig = multipartConfig;
		}

		@Override
		public RegistrationBean createRegistrationBean(String name, Servlet source, int totalNumberOfSourceBeans) {
			String url = (totalNumberOfSourceBeans != 1) ? "/" + name + "/" : "/";
			if (name.equals(DISPATCHER_SERVLET_NAME)) {
				url = "/"; // always map the main dispatcherServlet to "/"
			}
			ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<>(source, url);
			bean.setName(name);
			bean.setMultipartConfig(this.multipartConfig);
			return bean;
		}

	}

	/**
	 * {@link RegistrationBeanAdapter} for {@link Filter} beans.
	 */
	private static class FilterRegistrationBeanAdapter implements RegistrationBeanAdapter<Filter> {

		@Override
		public RegistrationBean createRegistrationBean(String name, Filter source, int totalNumberOfSourceBeans) {
			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(source);
			bean.setName(name);
			return bean;
		}

	}

	/**
	 * {@link RegistrationBeanAdapter} for certain {@link EventListener} beans.
	 */
	private static class ServletListenerRegistrationBeanAdapter implements RegistrationBeanAdapter<EventListener> {

		@Override
		public RegistrationBean createRegistrationBean(String name, EventListener source,
				int totalNumberOfSourceBeans) {
			return new ServletListenerRegistrationBean<>(source);
		}

	}

}
