package smoketest.tomcat.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 自定义的条件类
 */
public class MyConditional implements Condition {

  private final Logger logger = LoggerFactory.getLogger(MyConditional.class);

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    logger.info("执行自定义的类条件信息");
    //容器中包含 myAspect 组件才返回Ture
    return context.getBeanFactory().containsBean("myAspect");
  }

}
