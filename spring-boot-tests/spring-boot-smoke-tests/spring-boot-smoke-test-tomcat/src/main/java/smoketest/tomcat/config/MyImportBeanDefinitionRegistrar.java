package smoketest.tomcat.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import smoketest.tomcat.dao.MyDao;

public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                      BeanDefinitionRegistry registry) {
    RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(MyDao.class);
    //把自定义的bean定义导入到容器中
    registry.registerBeanDefinition("myDao", rootBeanDefinition);
  }

}
