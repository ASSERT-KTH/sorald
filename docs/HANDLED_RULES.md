## Handled rules

> This file is generated using [handled_rules.py](/experimentation/tools/sorald/handled_rules.py).
> Please refrain from editing it manually.

Sorald can currently repair violations of the following rules:

* [Bug](#bug)

    * [".equals()" should not be used to test the values of "Atomic" classes](#equals-should-not-be-used-to-test-the-values-of-atomic-classes-sonar-rule-2204) ([Sonar Rule 2204](https://rules.sonarsource.com/java/RSPEC-2204))
    * ["BigDecimal(double)" should not be used](#bigdecimaldouble-should-not-be-used-sonar-rule-2111) ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))
    * ["compareTo" should not return "Integer.MIN_VALUE"](#compareto-should-not-return-integermin_value-sonar-rule-2167) ([Sonar Rule 2167](https://rules.sonarsource.com/java/RSPEC-2167))
    * ["equals(Object obj)" should test argument type](#equalsobject-obj-should-test-argument-type-sonar-rule-2097) ([Sonar Rule 2097](https://rules.sonarsource.com/java/RSPEC-2097))
    * ["getClass" should not be used for synchronization](#getclass-should-not-be-used-for-synchronization-sonar-rule-3067) ([Sonar Rule 3067](https://rules.sonarsource.com/java/RSPEC-3067))
    * ["hashCode" and "toString" should not be called on array instances](#hashcode-and-tostring-should-not-be-called-on-array-instances-sonar-rule-2116) ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))
    * ["InterruptedException" should not be ignored](#interruptedexception-should-not-be-ignored-sonar-rule-2142) ([Sonar Rule 2142](https://rules.sonarsource.com/java/RSPEC-2142))
    * ["Iterator.next()" methods should throw "NoSuchElementException"](#iteratornext-methods-should-throw-nosuchelementexception-sonar-rule-2272) ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))
    * ["Thread.run()" should not be called directly](#threadrun-should-not-be-called-directly-sonar-rule-1217) ([Sonar Rule 1217](https://rules.sonarsource.com/java/RSPEC-1217))
    * ["toString()" and "clone()" methods should not return null](#tostring-and-clone-methods-should-not-return-null-sonar-rule-2225) ([Sonar Rule 2225](https://rules.sonarsource.com/java/RSPEC-2225))
    * [Exception should not be created without being thrown](#exception-should-not-be-created-without-being-thrown-sonar-rule-3984) ([Sonar Rule 3984](https://rules.sonarsource.com/java/RSPEC-3984))
    * [JEE applications should not "getClassLoader"](#jee-applications-should-not-getclassloader-sonar-rule-3032) ([Sonar Rule 3032](https://rules.sonarsource.com/java/RSPEC-3032))
    * [Math operands should be cast before assignment](#math-operands-should-be-cast-before-assignment-sonar-rule-2184) ([Sonar Rule 2184](https://rules.sonarsource.com/java/RSPEC-2184))
    * [Math should not be performed on floats](#math-should-not-be-performed-on-floats-sonar-rule-2164) ([Sonar Rule 2164](https://rules.sonarsource.com/java/RSPEC-2164))
    * [Resources should be closed](#resources-should-be-closed-sonar-rule-2095) ([Sonar Rule 2095](https://rules.sonarsource.com/java/RSPEC-2095))
    * [Strings and Boxed types should be compared using "equals()"](#strings-and-boxed-types-should-be-compared-using-equals-sonar-rule-4973) ([Sonar Rule 4973](https://rules.sonarsource.com/java/RSPEC-4973))
    * [Synchronization should not be based on Strings or boxed primitives](#synchronization-should-not-be-based-on-strings-or-boxed-primitives-sonar-rule-1860) ([Sonar Rule 1860](https://rules.sonarsource.com/java/RSPEC-1860))
    * [Variables should not be self-assigned](#variables-should-not-be-self-assigned-sonar-rule-1656) ([Sonar Rule 1656](https://rules.sonarsource.com/java/RSPEC-1656))

* [Code Smell](#code-smell)

    * ["Collections.EMPTY_LIST", "EMPTY_MAP", and "EMPTY_SET" should not be used](#collectionsempty_list-empty_map-and-empty_set-should-not-be-used-sonar-rule-1596) ([Sonar Rule 1596](https://rules.sonarsource.com/java/RSPEC-1596))
    * ["public static" fields should be constant](#public-static-fields-should-be-constant-sonar-rule-1444) ([Sonar Rule 1444](https://rules.sonarsource.com/java/RSPEC-1444))
    * ["ThreadLocal.withInitial" should be preferred](#threadlocalwithinitial-should-be-preferred-sonar-rule-4065) ([Sonar Rule 4065](https://rules.sonarsource.com/java/RSPEC-4065))
    * [Collection.isEmpty() should be used to test for emptiness](#collectionisempty-should-be-used-to-test-for-emptiness-sonar-rule-1155) ([Sonar Rule 1155](https://rules.sonarsource.com/java/RSPEC-1155))
    * [Every class implementing Serializable should declare a static final serialVersionUID.](#every-class-implementing-serializable-should-declare-a-static-final-serialversionuid-sonar-rule-2057) ([Sonar Rule 2057](https://rules.sonarsource.com/java/RSPEC-2057))
    * [Fields in a "Serializable" class should either be transient or serializable](#fields-in-a-serializable-class-should-either-be-transient-or-serializable-sonar-rule-1948) ([Sonar Rule 1948](https://rules.sonarsource.com/java/RSPEC-1948))
    * [Strings literals should be placed on the left side when checking for equality](#strings-literals-should-be-placed-on-the-left-side-when-checking-for-equality-sonar-rule-1132) ([Sonar Rule 1132](https://rules.sonarsource.com/java/RSPEC-1132))
    * [Unused "private" fields should be removed](#unused-private-fields-should-be-removed-sonar-rule-1068) ([Sonar Rule 1068](https://rules.sonarsource.com/java/RSPEC-1068))
    * [Unused assignments should be removed](#unused-assignments-should-be-removed-sonar-rule-1854) ([Sonar Rule 1854](https://rules.sonarsource.com/java/RSPEC-1854))
    * [Unused local variables should be removed](#unused-local-variables-should-be-removed-sonar-rule-1481) ([Sonar Rule 1481](https://rules.sonarsource.com/java/RSPEC-1481))
    * [Utility classes should not have public constructors](#utility-classes-should-not-have-public-constructors-sonar-rule-1118) ([Sonar Rule 1118](https://rules.sonarsource.com/java/RSPEC-1118))

* [Vulnerability](#vulnerability)

    * [XML parsers should not be vulnerable to XXE attacks](#xml-parsers-should-not-be-vulnerable-to-xxe-attacks-sonar-rule-2755) ([Sonar Rule 2755](https://rules.sonarsource.com/java/RSPEC-2755))

### *Bug*

#### ".equals()" should not be used to test the values of "Atomic" classes ([Sonar Rule 2204](https://rules.sonarsource.com/java/RSPEC-2204))

Any equality comparison between `AtomicInteger`, `AtomicLong`, or `AtomicBoolean` objects using the method `equals`, i.e., `obj1.equals(obj2)`, is replaced by a binary operator of the kind equals, where the left and right-hand operands are calls to the method `.get()` using both objects.

Example:
```diff
 		AtomicInteger aInt1 = new AtomicInteger(0);
 		AtomicInteger aInt2 = new AtomicInteger(0);
-		isEqual = aInt1.equals(aInt2); // Noncompliant
+		isEqual = aInt1.get() == aInt2.get();
```


-----

#### "BigDecimal(double)" should not be used ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))

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


-----

#### "compareTo" should not return "Integer.MIN_VALUE" ([Sonar Rule 2167](https://rules.sonarsource.com/java/RSPEC-2167))

Returning `Integer.MIN_VALUE` can cause errors because the return value of `compareTo` is sometimes inversed, with the expectation that negative values become positive. However, inversing `Integer.MIN_VALUE` yields `Integer.MIN_VALUE` rather than `Integer.MAX_VALUE`. Any `return Integer.MIN_VALUE` in a `compareTo` method is then replaced by `return -1`.

```diff
-  public int compareTo(CompareToReturnValue a) {
-    return Integer.MIN_VALUE; // Noncompliant
-  }
+  public int compareTo(CompareToReturnValue a) {
+     return -1;
+  }
```


-----

#### "equals(Object obj)" should test argument type ([Sonar Rule 2097](https://rules.sonarsource.com/java/RSPEC-2097))

This repair adds a type test to any `equals(Object)` method that lacks such.

Example:

```diff
    @Override
    public boolean equals(Object obj) {
+       if (obj == null || getClass() != obj.getClass()) {
+               return false;
+       }
        return toFile().equals(((SpoonResource) obj).toFile());
    }
```


-----

#### "getClass" should not be used for synchronization ([Sonar Rule 3067](https://rules.sonarsource.com/java/RSPEC-3067))

Any invocation using getClass will be type-checked if the object's invoked by `getClass` is final or an enum. If not, the invocation will be transformed to `.class` instead of `getClass`.

Example:
```diff
class SynchronizationOnGetClass {
-  public void method1() {
-    InnerClass i = new InnerClass();
-    synchronized (i.getObject().getClass()) { // Noncompliant - object's modifier is unknown, assume non-final nor enum
-  }
+  public void method1() {
+    InnerClass i = new InnerClass();
+    synchronized(Object.class) {}
+  }
-  public void method2() {
-    synchronized (getClass()) {}
-  }
+  public void method2() {
+    synchronized(SynchronizationOnGetClass.class) {}
+  }
}
```


-----

#### "hashCode" and "toString" should not be called on array instances ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))

Any invocation of `toString()` or `hashCode()` on an array is replaced with `Arrays.toString(parameter)` or `Arrays.hashCode(parameter)`.

Example:
```diff
-        String argStr = args.toString();// Noncompliant
-        int argHash = args.hashCode();// Noncompliant
+        String argStr = Arrays.toString(args);
+        int argHash = Arrays.hashCode(args);
```

Check out an accepted PR in [Spoon](https://github.com/INRIA/spoon/pull/3134) that repairs one ArrayHashCodeAndToString violation.


-----

#### "InterruptedException" should not be ignored ([Sonar Rule 2142](https://rules.sonarsource.com/java/RSPEC-2142))

A `catch` block that catches an `InterruptedException`, but neither re-interrupts the method nor rethrows the `InterruptedException`, i.e., ignores the `InterruptedException`, is augmented with `Thread.currentThread().interrupt();`.

Example:
```diff
    public void run() {
        try {
            while (true) {
                // do stuff
            }
-       } catch (InterruptedException e) { // Noncompliant; logging is not enough
+       } catch (InterruptedException e) {
            LOGGER.log(Level.WARN, "Interrupted!", e);
+           Thread.currentThread().interrupt();
        }
    }
```

Sorald places the interrupt as late as possible in the catch block, but before any throws or returns.

If there are multiple exceptions handled in a single catch block, a new catch block is added.

Example:

```diff
public static void method() {
    try {
        if (1 < 2) {
            throw new ExecutionException(new RuntimeException());
        } else {
            throw new InterruptedException();
        }
    }
    // Noncompliant
-   catch (InterruptedException | ExecutionException e) {
+   catch (ExecutionException e) {
        throw new RuntimeException(e);
    }
+   catch (InterruptedException e) {
+       Thread.currentThread().interrupt();
+       throw new RuntimeException(e);
+   }
}
```


-----

#### "Iterator.next()" methods should throw "NoSuchElementException" ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))

Any implementation of the `Iterator.next()` method that does not throw `NoSuchElementException` has a code snippet added to its start. The code snippet consists of a call to `hasNext()` and a throw of the error.

Example:
```diff
+import java.util.NoSuchElementException;

public class IteratorNextException implements Iterator {
...

@Override -    public String next() { // Noncompliant
+    public String next() {
+        if (!hasNext()) {
+            throw new NoSuchElementException();
+        }
...
}
```

Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/75) that repairs two IteratorNextException violations.


-----

#### "Thread.run()" should not be called directly ([Sonar Rule 1217](https://rules.sonarsource.com/java/RSPEC-1217))

Sorald fixes the violations of this rule by replacing each invocation of `Thread.run()` with an invocation of `Thread.start()`.

Example:
```diff
    Thread myThread = new Thread(runnable);
-   myThread.run();
+   myThread.start();
```


-----

#### "toString()" and "clone()" methods should not return null ([Sonar Rule 2225](https://rules.sonarsource.com/java/RSPEC-2225))

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


-----

#### Exception should not be created without being thrown ([Sonar Rule 3984](https://rules.sonarsource.com/java/RSPEC-3984))

Throw a `Throwable` that has been created but not thrown.

Example:
```diff
        if (x < 0) {
-           new IllegalArgumentException("x must be nonnegative"); // Noncompliant {{Throw this exception or remove this useless statement}}
+           throw new IllegalArgumentException("x must be nonnegative");
        }
```


-----

#### JEE applications should not "getClassLoader" ([Sonar Rule 3032](https://rules.sonarsource.com/java/RSPEC-3032))

Using the standard `getClassLoader()` may not return the right class loader in Java Enterprise Edition context. Instead, `getClassLoader()` usage such as `this.getClass().getClassLoader()` and `Dummy.class.getClassLoader()` should be replaced by `Thread.currentThread().getContextClassLoader()`. In particular, such replacement only occurs if the `.java` file uses the `javax` package in its imports.

Example:
```diff
-    ClassLoader d = this.getClass().getClassLoader(); // Noncompliant
+    ClassLoader d = Thread.currentThread().getContextClassLoader();
-    Dummy.class.getClassLoader().loadClass("anotherclass"); // Noncompliant
+    Thread.currentThread().getContextClassLoader().loadClass("anotherclass");
```


-----

#### Math operands should be cast before assignment ([Sonar Rule 2184](https://rules.sonarsource.com/java/RSPEC-2184))

In arithmetic expressions, when the operands are `int` and/or `long`, but the result of the expression is assigned to
a `long`, `double`, or `float`, the first left-hand is cast to the final type before the operation takes place.
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


-----

#### Math should not be performed on floats ([Sonar Rule 2164](https://rules.sonarsource.com/java/RSPEC-2164))

In arithmetic expressions between two `float`s, both left and right operands are cast to `double`.

Example:
```diff
         float a = 16777216.0f;
         float b = 1.0f;
-        float c = a + b; // Noncompliant, yields 1.6777216E7 not 1.6777217E7
+        float c = (double) a + (double) b;
```


-----

#### Resources should be closed ([Sonar Rule 2095](https://rules.sonarsource.com/java/RSPEC-2095))

The repair encloses the parent block of resource initialization in a try-with resources.
If it was already in a try block it replaces the try with try-with-resources instead
of creating a new one, so that useless nested try blocks are not created.

Example:
```diff
-            ZipInputStream zipInput = null;
-            try {
-                zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));// Noncompliant
+            try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                 ZipEntry entry;
...
             } catch (Exception e) {
                 Launcher.LOGGER.error(e.getMessage(), e);
             }
```


-----

#### Strings and Boxed types should be compared using "equals()" ([Sonar Rule 4973](https://rules.sonarsource.com/java/RSPEC-4973))

Any comparison of strings or boxed types using `==` or `!=` is replaced by `equals`.

Example:
```diff
-        if (firstName == lastName) // Noncompliant
+        if (firstName.equals(lastName))
...
-        return b != a; // Noncompliant
+        return !b.equals(a);
```

Check out an accepted PR in [Apache Sling Discovery](https://github.com/apache/sling-org-apache-sling-discovery-impl/pull/1) that repairs one CompareStringsBoxedTypesWithEquals violation.


-----

#### Synchronization should not be based on Strings or boxed primitives ([Sonar Rule 1860](https://rules.sonarsource.com/java/RSPEC-1860))

Objects which are pooled, such as Strings or boxed primitives, and potentially reused should not be used for synchronization, since they can cause deadlocks. The transformation will do the following. If the lock is a field of the current class where the synchronization block is in, then it will simply add a new field as an `Object` lock. If the lock is obtained from another object through the `get` method, it will add a new field for the new `Object` lock and a new method to get the object.

Example:
```diff
   private final Boolean bLock = Boolean.FALSE;
+  private final Object bLockLegal = new Object();
   private final InnerClass i = new InnerClass();
-  void method1() {
-    synchronized(bLock) {}
-    synchronized(i.getLock()){}
-  }
+  void method1() {
+    synchronized(bLockLegal) {}
+    synchronized(i.getLockLegal()){}
+  }
   class InnerClass {
        public Boolean innerLock = Boolean.FALSE;
+       public Object innerLockLegal = new Object();

        public Boolean getLock() {
            return this.innerLock;
        }
+       public Object getLockLegal() {
+           return this.innerLockLegal;
+       }
  }
```


-----

#### Variables should not be self-assigned ([Sonar Rule 1656](https://rules.sonarsource.com/java/RSPEC-1656))

Any assignment with identical left and right expressions will be processed. If the identifier being used in the self-assignment exists as both a local variable and a field, then the left expression will be changed by adding `this.` at the beginning of the expression. In any other case, including cases where there are invocations or access to another class field, such as `objectA.b = objectA.b`, the assignment will be removed.

Example:
```diff
class SelfAssignement {
   int a,c = 0;
   int[] b = {0};
   int h = 0;
   int[] g = 0;
   SelfAssignementCheckB checkB = new SelfAssignementCheckB();
-  void method(int e,int h) {
-    a = a; // Noncompliant [[sc=7;ec=8]] {{Remove or correct this useless self-assignment.}}
-    this.a = this.a; // Noncompliant
-    b[0] = b[0]; // Noncompliant
-    b[fun()] = b[fun()]; // Noncompliant
-    int d = 0;
-    d = d; // Noncompliant
-    e = e; // Noncompliant
     int[] g = new int[]{ 0 };
-    g[fun()] = g[fun()]; // Noncompliant
-    h = h;
-    checkB.b = checkB.b; // Noncompliant
-    checkB.getSelf().foo = checkB.getSelf().foo; // Noncompliant
-  }
+  void method(int e,int h) {
+    int d = 0;
     int[] g = new int[]{ 0 };
+    this.g[fun()] = g[fun()];
+    this.h = h;
+  }
   int fun() {
     return 0;
   }
}
```


-----

### *Code Smell*

#### "Collections.EMPTY_LIST", "EMPTY_MAP", and "EMPTY_SET" should not be used ([Sonar Rule 1596](https://rules.sonarsource.com/java/RSPEC-1596))

The `EMPTY_...` fields from `Collections` return raw types, so they are replaced by the `empty...()` methods that return generic ones.

Example:
```diff
- List<String> collection1 = Collections.EMPTY_LIST;  // Noncompliant
- Map<String, String> collection2 = Collections.EMPTY_MAP;  // Noncompliant
- Set<String> collection3 = Collections.EMPTY_SET;  // Noncompliant
+ List<String> collection1 = Collections.emptyList();
+ Map<String, String> collection2 = Collections.emptyMap();
+ Set<String> collection3 = Collections.emptySet();
```


-----

#### "public static" fields should be constant ([Sonar Rule 1444](https://rules.sonarsource.com/java/RSPEC-1444))

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


-----

#### "ThreadLocal.withInitial" should be preferred ([Sonar Rule 4065](https://rules.sonarsource.com/java/RSPEC-4065))

Sorald fixes the violations of this rule by replacing an anonymous `ThreadLocal` class overriding `initalValue` with an invocation of `ThreadLocal.withInitial(Supplier)`.
Example:
```diff
-    ThreadLocal<String> myThreadLocal = new ThreadLocal<String>() {
-       @Override
-       protected String initialValue() {
-           return "Hello";
-       }
+ Threadlocal<String> myThreadLocal = ThreadLocal.withInitial(() -> "Hello");
```

-----

#### Collection.isEmpty() should be used to test for emptiness ([Sonar Rule 1155](https://rules.sonarsource.com/java/RSPEC-1155))

Using `Collection.size()` to test for emptiness works, but using `Collection.isEmpty()` makes the code more readable and
can be more performant. Expressions `myCollection.size() == 0` are replaced by `myCollection.isEmpty()`, and
expressions `myCollection.size() != 0` are replaced by `!myCollection.isEmpty()`.

Example:
```diff
- if (myCollection.size() == 0) {  // Noncompliant
+ if (myCollection.isEmpty()) {
...
- if (myCollection.size() != 0) {  // Noncompliant
+ if (!myCollection.isEmpty()) {
```


-----

#### Every class implementing Serializable should declare a static final serialVersionUID. ([Sonar Rule 2057](https://rules.sonarsource.com/java/RSPEC-2057))

The repair consists of add a serialVersionUID to classes implementing Serializable
Example:
```diff
public class NoUID implements Serializable {
+  private static final long serialVersionUID = 1L;
}
```


-----

#### Fields in a "Serializable" class should either be transient or serializable ([Sonar Rule 1948](https://rules.sonarsource.com/java/RSPEC-1948))

The repair adds the modifier `transient` to all non-serializable
fields. In the future, the plan is to give user the option if they want to go to the class
of that field and add `implements Serializable` to it.

Example:
```diff
 public class SerializableFieldProcessorTest implements Serializable {
-    private Unser uns;// Noncompliant
+    private transient Unser uns;
```

Check out an accepted PR in [Spoon](https://github.com/INRIA/spoon/pull/2121) that repairs three SerializableFieldInSerializableClass violations.


-----

#### Strings literals should be placed on the left side when checking for equality ([Sonar Rule 1132](https://rules.sonarsource.com/java/RSPEC-1132))

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


-----

#### Unused "private" fields should be removed ([Sonar Rule 1068](https://rules.sonarsource.com/java/RSPEC-1068))

The repair consists of deleting unused `private` fields as it is considered as dead code.

Example:

```diff
 public class UnusedPrivateField {
-    private String a = "Hello world!";

     public int compute(int number) {
         return number * 42;
     }
 }
```

However, the `serialVersionUID` field, which must be `private`, `static`, `final`, and of type `long`, in Serializable
classes is not deleted because it is used during deserialization of byte stream.


-----

#### Unused assignments should be removed ([Sonar Rule 1854](https://rules.sonarsource.com/java/RSPEC-1854))

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


-----

#### Unused local variables should be removed ([Sonar Rule 1481](https://rules.sonarsource.com/java/RSPEC-1481))

The repair consists of deleting unused local variables. This largely overlaps
with rule 1854 "Unused assignments should be removed", but covers some
additional cases. For example, loop header variables are not considered by
rule 1854 for some reason, but they are included in this rule.

Example repair where a variable declared in a loop header is unused:

```diff
 public static void main(String[] args) {
-    for (int x = 0, y = 0, z = 10; x <= z; x++) { // Noncompliant, y is not used
+    for (int x = 0, z = 10; x <= z; x++) { // Noncompliant, y is not used
        System.out.println("Current: " + x);
        System.out.println("Goal: " + z);
    }
}
```


-----

#### Utility classes should not have public constructors ([Sonar Rule 1118](https://rules.sonarsource.com/java/RSPEC-1118))

This repair targets classes that only have static members (_utility classes_)
and a public constructor, which they should not have according to the rule.
This repair only targets the case when there is an _implicit_ public
constructor, the so-called _default_ constructor, which is generated by the
compiler when no constructor is specified. The repair simply consists of adding
a private, no-args constructor at the top of the class.

```diff
 public class UtilityClassWithoutConstructor {
+    private UtilityClassWithoutConstructor() {
+    }
     public static final int SOME_CONSTANT = 42;
 }
```

> **Important:** The repair does not attempt to do anything if there is an
> explicit public constructor.


-----

### *Vulnerability*

#### XML parsers should not be vulnerable to XXE attacks ([Sonar Rule 2755](https://rules.sonarsource.com/java/RSPEC-2755))

**Note:** This processor is a work in progress!

This repair is a work in progress. On a high level, it aims to make XML parsing
safe against XXE attacks by disabling features such as external schema and DTD
support.

Currently, we target the following types:

* `DocumentBuilderFactory`
* `TransformerFactory`
* `XMLInputFactory`

The transformation is highly similar regardless of type, and consists of replacing
factory creation with a call to a helper method that creates a "safe" factory. For
`DocumentBuilderFactory`, it looks like the following:

```diff
         // somewhere in a method body
-        DocumentBuilder builder = DocumentBuilderFactory.newInstance().createDocumentBuilder();
+        DocumentBuilder builder = createDocumentBuilderFactory().createDocumentBuilder();
         [...]

         // somewhere in a method body
-        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
+        DocumentBuilderFactory df = createDocumentBuilderFactory();
         [...]

+    private static DocumentBuilderFactory createDocumentBuilderFactory() {
+         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
+         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
+         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
+         return factory;
+     }
```

The precise attributes set in `createTYPEFACTORY` depends on the particular
factory used. For example, with `TransformerFactory`, `ACCESS_EXTERNAL_SCHEMA`
is replaced with `ACCESS_EXTERNAL_STYLESHEET`. The method name to set attributes
also varies, but is typically either `setAttribute` or `setProperty`.

This is just a small part of rule 2755, and we are working on adding support
for other cases. The repair currently cannot handle builders and factories in
fields, as Sonar does not appear to issue warnings for them.


-----
