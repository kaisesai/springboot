package smoketest.tomcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import smoketest.tomcat.condition.MyConditional;
import smoketest.tomcat.select.MySelector;

@Configuration
@Import(value = {MySelector.class, MyImportBeanDefinitionRegistrar.class})
public class MyConfig {

  // @Bean
  public MyAspect myAspect() {
    System.out.println("MyAspect 组件自动装配到容器中");
    return new MyAspect();
  }

  @Bean
  @Conditional(value = MyConditional.class)
  public MyLog myLog() {
    System.out.println("MyLog 组件自动装配到容器中");
    return new MyLog();
  }

}
