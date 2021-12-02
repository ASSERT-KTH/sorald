Constructor of `BigDecimal` that has *exactly* one parameter of type, either `float` or `double`, is replaced with an invocation of the `BigDecimal.valueOf(parameter)` method.

Example:
```diff
         double d = 1.1;
         float f = 2.2;
-        BigDecimal bd1 = new BigDecimal(d);// Noncompliant
-        BigDecimal bd2 = new BigDecimal(1.1); // Noncompliant
-        BigDecimal bd3 = new BigDecimal(f); // Noncompliant
+        BigDecimal bd1 = BigDecimal.valueOf(d);
+        BigDecimal bd2 = BigDecimal.valueOf(1.1);
+        BigDecimal bd3 = BigDecimal.valueOf(f);
```

When the constructor of `BigDecimal` is called with two or more arguments, the first argument is enclosed in a string if it is of type `float` or `double`.

Example:
```diff
        MathContext mc;
-       BigDecimal bd4 = new BigDecimal(2.0, mc); // Noncompliant {{Enclose the first argument in a string.}}
-       BigDecimal bd6 = new BigDecimal(2.0f, mc); // Noncompliant {{Enclose the first argument in a string.}}
+       BigDecimal bd4 = new BigDecimal("2.0", mc);
+       BigDecimal bd6 = new BigDecimal("2.0", mc);
```

Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/76) that repairs one BigDecimalDoubleConstructor violation.
