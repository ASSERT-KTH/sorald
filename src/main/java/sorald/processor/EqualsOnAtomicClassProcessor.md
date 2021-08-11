Any equality comparison between `AtomicInteger`, `AtomicLong`, or `AtomicBoolean` objects using the method `equals`, i.e., `obj1.equals(obj2)`, is replaced by a binary operator of the kind equals, where the left and right hand operands are calls to the method `.get()` using both objects.

Example:
```diff
 		AtomicInteger aInt1 = new AtomicInteger(0);
 		AtomicInteger aInt2 = new AtomicInteger(0);
-		isEqual = aInt1.equals(aInt2); // Noncompliant
+		isEqual = aInt1.get() == aInt2.get();
```
