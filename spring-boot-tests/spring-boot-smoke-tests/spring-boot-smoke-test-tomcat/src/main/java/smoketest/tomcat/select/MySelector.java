package smoketest.tomcat.select;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

public class MySelector implements ImportSelector {

  @NonNull
  @Override
  public String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
    return new String[]{"smoketest.tomcat.service.HelloWorldService"};
  }

}
