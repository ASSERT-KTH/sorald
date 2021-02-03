public class CompareToReturnValue implements Comparable<CompareToReturnValue> {
  @Override
  public int compareTo(CompareToReturnValue a) {
    return Integer.MIN_VALUE; // Noncompliant [[sc=12;ec=29]] {{Simply return -1}}
  }

  public int compareTo() {
    return Short.MIN_VALUE; // Compliant
  }

  public int getMinValue() {
    return Integer.MIN_VALUE; // Compliant
  }

  public int compareTo(int a) {
    return -1; // Compliant
  }

  public boolean compareTo(Boolean a) {
    return a; // Compliant
  }

  public Long compareTo(Long a) {
    return Long.MIN_VALUE; // Compliant
  }
}

class C implements Comparable<C> {
  @Override
  public int compareTo(C c) {
    return Integer.MAX_VALUE; // Compliant
  }
}

class D implements Comparable<D> {
  @Override
  public int compareTo(D d) {
    return Integer.MIN_VALUE; // Noncompliant
  }
}
