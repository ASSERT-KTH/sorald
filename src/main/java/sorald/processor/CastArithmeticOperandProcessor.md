In arithmetic expressions, when the operands are `int` and/or `long`, but the result of the expression is assigned to
a `long`, `double`, or `float`, the first left-hand is casted to the final type before the operation takes place.
To the extent possible, literal suffixes (such as `f` for `float`) are used instead of casting literals.

Example:
```diff
-    float twoThirds = 2/3; // Noncompliant; int division. Yields 0.0
+    float twoThirds = 2f/3;
...
     public long multiply(int lhs, int rhs){
-        return lhs * rhs; // Noncompliant, won't produce the expected results if lhs * rhs overflows an int
+        return (long) lhs * rhs;
     }
```
