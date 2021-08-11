For the return statements inside "toString()", this processor replaces the return expression with an empty string.

Note that this processor is incomplete and does not fix null-returning `clone()` methods.

Example:
```diff
public String toString() {
   Random r = new Random();
   if(r.nextInt(10) == r.nextInt(10)){
-     return null; // Noncompliant
+     return ""; // Noncompliant
   }
   else if(r.nextInt(10) == r.nextInt(10)){
      return "null";
   }
   return "";
}
```
