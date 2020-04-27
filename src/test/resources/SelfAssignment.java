class SelfAssignment {
  int a,c = 0;
  int e = 0;
  int f = 0;
  int[] b = {0};
  int[] g = {};

  void method3(int e,int[] g) {
    a = a; // Noncompliant
    this.a = this.a; // Noncompliant
    this.a = a;
    b[fun()] = b[fun()]; // Noncompliant

    int d = 0;
    d = d; // Noncompliant

    int f = 0;
    f = f;

    e = e; // Noncompliant

    g[fun()] = g[fun()];
  }

  int fun(){
    return 0;
  }
}
