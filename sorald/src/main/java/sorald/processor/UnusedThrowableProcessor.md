Throw a `Throwable` that has been created but not thrown.

Example:
```diff
        if (x < 0) {
-           new IllegalArgumentException("x must be nonnegative"); // Noncompliant {{Throw this exception or remove this useless statement}}
+           throw new IllegalArgumentException("x must be nonnegative");
        }
```
