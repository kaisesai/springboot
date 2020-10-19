package smoketest.tomcat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import smoketest.tomcat.filter.MyFilter;
import smoketest.tomcat.interceptor.MyInterceptor;

import javax.servlet.Filter;
import java.util.List;

/**
 * 自定义的 mvc 配置器
 */
@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

  @Autowired
  private MyInterceptor myInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    System.out.println("添加了拦截器：" + registry);
    // 添加拦截器
    registry.addInterceptor(myInterceptor)
      // 添加映射请求
      .addPathPatterns("/**")
      // 排除映射
      .excludePathPatterns("/index.html", "/");
  }

  // @Bean
  // public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
  //   return new MappingJackson2HttpMessageConverter();
  // }

  // @Override
  // public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
  //   // 添加 jackson 解析器
  //   converters.add(new MappingJackson2HttpMessageConverter());
  // }

  /**
   * 注册一个filter * @return
   */
  @Bean
  public FilterRegistrationBean<Filter> myFilter() {
    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new MyFilter());
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;
  }

}
