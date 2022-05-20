Any assignment with identical left and right expressions will be processed. If the identifier being used in the self-assignment exists as both a local variable and a field, then the left expression will be changed by adding `this.` at the beginning of the expression. In any other case, including cases where there are invocations or access to another class field, such as `objectA.b = objectA.b`, the assignment will be removed.

Example:
```diff
class SelfAssignement {
   int a,c = 0;
   int[] b = {0};
   int h = 0;
   int[] g = 0;
   SelfAssignementCheckB checkB = new SelfAssignementCheckB();
-  void method(int e,int h) {
-    a = a; // Noncompliant [[sc=7;ec=8]] {{Remove or correct this useless self-assignment.}}
-    this.a = this.a; // Noncompliant
-    b[0] = b[0]; // Noncompliant
-    b[fun()] = b[fun()]; // Noncompliant
-    int d = 0;
-    d = d; // Noncompliant
-    e = e; // Noncompliant
     int[] g = new int[]{ 0 };
-    g[fun()] = g[fun()]; // Noncompliant
-    h = h;
-    checkB.b = checkB.b; // Noncompliant
-    checkB.getSelf().foo = checkB.getSelf().foo; // Noncompliant
-  }
+  void method(int e,int h) {
+    int d = 0;
     int[] g = new int[]{ 0 };
+    this.g[fun()] = g[fun()];
+    this.h = h;
+  }
   int fun() {
     return 0;
   }
}
```
