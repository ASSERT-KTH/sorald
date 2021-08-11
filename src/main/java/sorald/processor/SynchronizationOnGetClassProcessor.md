Any invocation using getClass will be typechecked if the object's invoked by `getClass` is final or an enum. If not, the invocation will be transformed to `.class` instead of `getClass`.

Example:
```diff
class SynchronizationOnGetClass {
-  public void method1() {
-    InnerClass i = new InnerClass();
-    synchronized (i.getObject().getClass()) { // Noncompliant - object's modifier is unknown, assume non-final nor enum
-  }
+  public void method1() {
+    InnerClass i = new InnerClass();
+    synchronized(Object.class) {}
+  }
-  public void method2() {
-    synchronized (getClass()) {}
-  }
+  public void method2() {
+    synchronized(SynchronizationOnGetClass.class) {}
+  }
}
```
