The repair consists of making public static fields final.

Example:
```diff
 public class NonFinalPublicStaticField {
-    public static Integer meaningOfLife = 42;
+    public static final Integer meaningOfLife = 42;
     private static Integer CATCH = 22; // Compliant
     protected static Integer order = 66; // Compliant
     static Integer roadToHill = 30; // Compliant
 }
```
