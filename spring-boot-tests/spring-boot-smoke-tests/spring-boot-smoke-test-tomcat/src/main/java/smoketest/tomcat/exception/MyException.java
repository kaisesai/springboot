package smoketest.tomcat.exception;

/**
 * 自定义的异常
 */
public class MyException extends RuntimeException {

  private String code;

  private int msg;

  public MyException(String code, int msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getCode() {
    return code;
  }

  public int getMsg() {
    return msg;
  }

}
