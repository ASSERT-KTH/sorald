In arithmetic expressions between two `float`s, one of the operands are cast to `double`.

Example:
```diff
        float a = 16777216.0f;
        float b = 1.0f;
-       double d1 = a + b;
+       double d1 = (double) a + b;
```

Note that this processor is incomplete as it does not perform the following
repair even though it is recommended by SonarSource in their
[documentation](https://rules.sonarsource.com/java/RSPEC-2164):
```diff
         float a = 16777216.0f;
         float b = 1.0f;
-        float c = a + b; // Noncompliant, yields 1.6777216E7 not 1.6777217E7
+        float c = (double) a + (double) b;
```

The reason we do not perform this repair is that it produces a non-compilable
code. See [#570](https://github.com/SpoonLabs/sorald/issues/570) for more
details.
