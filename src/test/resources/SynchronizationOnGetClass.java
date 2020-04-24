class SynchronizationOnGetClass {
  final class MemberSelect {
    public void memberSelectOnUnknownSymbol() {
      synchronized (this.getClass()) { // Compliant
      }
    }
  }

  class Coverage {
    public void unrelatedSynchronizedExpr() {
      Object monitor = new Object();
      synchronized (monitor) { // Compliant

      }
    }
  }

  class InnerClass {
    public Object getObject() {
      Object o = new Object();
      return o; 
    }
  }

  public void method1() {
    InnerClass i = new InnerClass();
    synchronized (i.getObject().getClass()) { // Noncompliant - object's modifier is unknown, assume non-final nor enum
    }
  }

  public void method2() {
    synchronized (getClass()) { // Noncompliant
    }
  }
}
