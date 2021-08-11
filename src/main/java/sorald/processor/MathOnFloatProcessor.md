In arithmetic expressions between two `float`s, both left and right operands are casted to `double`.

Example:
```diff
         float a = 16777216.0f;
         float b = 1.0f;
-        float c = a + b; // Noncompliant, yields 1.6777216E7 not 1.6777217E7
+        float c = (double) a + (double) b;
```
