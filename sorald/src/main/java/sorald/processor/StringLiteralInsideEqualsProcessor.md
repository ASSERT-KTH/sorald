String comparisons by using `equals` or `equalsIgnoreCase` in which the target for the method call is a variable and the argument is a literal are changed by swapping the variable and the literal. This avoids potential null pointer exceptions.

Example:
```diff
- System.out.println("Equal? " + myString.equals("foo")); // Noncompliant; can raise a NPE
+ System.out.println("Equal? " + "foo".equals(myString));
```

When there is a null check on the variable, it is removed.

Example:
```diff
- System.out.println("Equal? " + (myString != null && myString.equals("foo"))); // Noncompliant
+ System.out.println("Equal? " + "foo".equals(myString));
```
