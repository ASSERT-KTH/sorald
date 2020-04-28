class SelfAssignement {
  int a,c = 0;
  int[] b = {0};
  int h = 0;
  int[] g = 0;

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
  }


  int fun(){
    return 0;
  }
}
