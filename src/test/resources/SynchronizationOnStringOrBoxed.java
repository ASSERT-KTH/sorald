class SynchronizationOnStringOrBoxed {
  
  private final Boolean bLock = Boolean.FALSE;
  private final Integer iLock = Integer.valueOf(0);
  final String sLock = "LOCK";
  private final Object oLock = new Object();
  private final InnerClass i = new InnerClass();

  class InnerClass {
    public Boolean innerLock = Boolean.FALSE;
    private SynchronizationOnStringOrBoxed outerClass = new SynchronizationOnStringOrBoxed();

    public void method2() {
      synchronized(this.outerClass.sLock) {  // Noncompliant
        // ...
      }
    }

    public Boolean getLock() {
      return this.innerLock;
    }
  }

  void method1() {
    
    synchronized(bLock) {  // Noncompliant [[sc=18;ec=23]] {{Synchronize on a new "Object" instead.}}
      // ...
    }
    synchronized(iLock) {  // Noncompliant
      // ...
    }
    synchronized(sLock) {  // Noncompliant
      // ...
    }
    synchronized(oLock) { 
      // ...
    }
    synchronized(i.getLock()) { // Noncompliant

    }
  }
  
  void method3() {
    synchronized(sLock) {  // Noncompliant
      // ...
    }
    synchronized(i.getLock()) { // Noncompliant

    }
  }
}
