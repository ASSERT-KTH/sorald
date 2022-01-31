
public class ThreadLocalInitialWithExactlyOneReturnStatement {
  
  public void bar() {
    ThreadLocal<String> threadLocal = new ThreadLocal<String>() { // Noncompliant
      @Override
      protected String initialValue() {
        return "hello";
      }
    };
    threadLocal.set("42");
  }
}
