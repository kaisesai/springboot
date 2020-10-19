// package smoketest.tomcat.exception;
//
// import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
// import org.springframework.stereotype.Component;
// import org.springframework.web.context.request.WebRequest;
//
// import java.util.Map;
//
// @Component
// public class MyErrorAttribute extends DefaultErrorAttributes {
//
//   @Override
//   public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
//     //获取父类的封装字段结果
//     Map<String, Object> retInfo = super.getErrorAttributes(webRequest, includeStackTrace);
//     //获取全局异常自定义的结果
//     Map<String, Object> ext = (Map<String, Object>) webRequest.getAttribute("ext", 0);
//     //封装自定义的错误信息
//     retInfo.put("company", "my");
//     retInfo.put("ext", ext);
//     return retInfo;
//   }
//
// }
