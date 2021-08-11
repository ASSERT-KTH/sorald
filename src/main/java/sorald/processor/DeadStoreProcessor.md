The repair consists of deleting useless assignments.

Example:
```diff
     public void dead()
     {
         int x=5;
         int y=10;
         int z;
         y=x*x*y;
         System.out.println(y);
-        x=5;// Noncompliant
-        y=10;// Noncompliant
-        z=20;// Noncompliant
     }
```

In some cases, simply deleting a local variable declaration can lead to code
not compiling, even if the initializer for the variable is indeed a dead store.
Sorald then ensures that there is a declaration with the appropriate scope,
if possible merging with the closest variable write.

```diff
    public void dead() {
-       int a = 2; // Noncompliant
-       a = 3;
+       int a = 3;
        System.out.println(a);
    }
```

In some cases where the variable in question is used in different execution
paths, merging with the closest write isn't an option. Sorald then places a
declaration as close to the usages as possible.

```diff
    public void dead(int a, int b) {
-       int c = 2;
        if (a < b) {
+           int c;
            if (b < a) {
                c = a + b;
            } else {
                c = a - b;
            }
            System.out.println(c);
        }
    }
```

In cases where the assignment is an instance method invocation, Sorald
preserves the invocation so as not to inadvertently remove side effects of the
invocation.

```diff
    public List<Integer> concatenate(List<Integer> lhs, List<Integer> rhs) {
        List<Integer> base = new ArrayList<>(lhs);
-       boolean changed = rhs.addAll(lhs);
+       rhs.addAll(lhs);
        return rhs;
    }
```

Check out an accepted PR in [Spoon](https://github.com/INRIA/spoon/pull/2265) that repairs one DeadStore violation.
