package smoketest.tomcat.dao;

import java.util.ArrayList;
import java.util.List;

public class MyDao {

  public static final List<String> USER_NAME = new ArrayList<>();

  static {
    USER_NAME.add("小红");
    USER_NAME.add("小蓝");
    USER_NAME.add("小绿");
  }

  public List<String> findAll() {
    return USER_NAME;
  }

}
