
public class ThreadLocalInitialWithMultipleStatements {
  
  public void bar() {
    ThreadLocal<String> threadLocal = new ThreadLocal<String>() { // Noncompliant
      @Override
      protected String initialValue() {
        String s = "hello";
        return s+"42";
      }
    };
    threadLocal.set("42");
  }
}
