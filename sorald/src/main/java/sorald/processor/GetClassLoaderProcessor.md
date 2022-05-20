Using the standard `getClassLoader()` may not return the right class loader in Java Enterprise Edition context. Instead, `getClassLoader()` usage such as `this.getClass().getClassLoader()` and `Dummy.class.getClassLoader()` should be replaced by `Thread.currentThread().getContextClassLoader()`. In particular, such replacement only occurs if the `.java` file uses the `javax` package in its imports.

Example:
```diff
-    ClassLoader d = this.getClass().getClassLoader(); // Noncompliant
+    ClassLoader d = Thread.currentThread().getContextClassLoader();
-    Dummy.class.getClassLoader().loadClass("anotherclass"); // Noncompliant
+    Thread.currentThread().getContextClassLoader().loadClass("anotherclass");
```
