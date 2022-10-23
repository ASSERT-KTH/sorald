Any comparison of strings or boxed types using `==` or `!=` is replaced by `equals`.
By default, we use `equals` on the left operand, so we add a null check before comparison.

Example:
```diff
-        if (firstName == lastName) // Noncompliant
+        if (firstName != null && firstName.equals(lastName))
...
-        return b != a; // Noncompliant
+        return b != null && !b.equals(a);
```

Check out an accepted PR in [Apache Sling Discovery](https://github.com/apache/sling-org-apache-sling-discovery-impl/pull/1) that repairs one CompareStringsBoxedTypesWithEquals violation.
