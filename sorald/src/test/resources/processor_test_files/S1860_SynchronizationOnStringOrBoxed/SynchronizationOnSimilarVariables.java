class SynchronizationOnSimilarVariables {

  class InnerClass1 {
    public Boolean isStopped = false;

    public void method() {
      synchronized(isStopped) {  // Noncompliant
        // ...
      }
    }
  }

  class InnerClass2 {
    public Boolean isStopped = false;

    public void method() {
      synchronized(isStopped) {  // Noncompliant
        // ...
      }
    }
  }
}
