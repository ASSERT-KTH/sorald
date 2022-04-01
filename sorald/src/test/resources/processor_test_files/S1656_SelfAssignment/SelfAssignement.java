class SelfAssignement {
  int a,c = 0;
  int[] b = {0};
  int h = 0;
  int[] g = {0};
  SelfAssignementCheckB checkB = new SelfAssignementCheckB();
  void method(int e,int h) {
    a = a; // Noncompliant [[sc=7;ec=8]] {{Remove or correct this useless self-assignment.}}
    this.a = this.a; // Noncompliant
    b[0] = b[0]; // Noncompliant
    b[fun()] = b[fun()]; // Noncompliant
    int d = 0;
    d = d; // Noncompliant
    e = e; // Noncompliant
    int[] g = {0};
    g[fun()] = g[fun()]; // Noncompliant
    h = h; // Noncompliant
    checkB.b = checkB.b; // Noncompliant
    checkB.getSelf().foo = checkB.getSelf().foo; // Noncompliant
  }


  int fun(){
    return 0;
  }

  class SelfAssignementCheckB {
    int b;
    int foo;

    void setFoo(int foo){
      this.foo = foo;
    }

    int getFoo() {
      return this.foo;
    }

    SelfAssignementCheckB getSelf() {
      return this;
    }

    SelfAssignementCheckB(SelfAssignementCheckB bInstance) {
      foo = bInstance.foo;
    }

    SelfAssignementCheckB() {}
  }
}
