package smoketest.tomcat.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * 自定义过滤器
 */
public class MyFilter extends GenericFilter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    System.out.println("MyFilter 的 doFilter 方法");
    chain.doFilter(request, response);
  }

}
