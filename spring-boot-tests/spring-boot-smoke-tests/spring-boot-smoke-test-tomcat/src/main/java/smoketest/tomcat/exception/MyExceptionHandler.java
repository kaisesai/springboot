package smoketest.tomcat.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义的异常处理器
 */
@ControllerAdvice
public class MyExceptionHandler {

  /**
   * 浏览器和其他客户端都返回了json 数组，不满足自适应
   *
   * @param e
   * @param request
   * @return
   */
  @ExceptionHandler(value = MyException.class)
  public String dealException(MyException e, HttpServletRequest request) {
    Map<String, Object> retInfo = new HashMap<>();
    retInfo.put("code", e.getCode());
    retInfo.put("msg", e.getMsg());

    // request.setAttribute("javax.servlet.error.exception", ex);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    request.setAttribute("javax.servlet.error.status_code",500);
    request.setAttribute("ext",retInfo);

    // return retInfo;
    return "forward:/error";
  }

}
