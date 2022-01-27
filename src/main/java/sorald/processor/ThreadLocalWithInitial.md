Sorald fixes the violations of this rule by replacing an anonymous `ThreadLocal` class overriding `initalValue` with an invocation of `ThreadLocal.withInitial(Supplier)`.
Example:
```diff
-    ThreadLocal<String> myThreadLocal = new ThreadLocal<String>() {
-       @Override
-       protected String initialValue() {
-           return "Hello";
-       }
+ Threadlocal<String> myThreadLocal = ThreadLocal.withInitial(() -> "Hello");
```