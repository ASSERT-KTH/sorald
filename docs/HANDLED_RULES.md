## Handled rules
Sonarqube-repair can currently repair violations of 15 rules of which 13 are labeled as `BUG` and 2 as `Code Smell`:

* [Bug](#bug)
    * [Resources should be closed](#resources-should-be-closed-sonar-rule-2095) ([Sonar Rule 2095](https://rules.sonarsource.com/java/RSPEC-2095))
    * ["BigDecimal(double)" should not be used](#bigdecimaldouble-should-not-be-used-sonar-rule-2111) ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))
    * ["hashCode" and "toString" should not be called on array instances](#hashcode-and-tostring-should-not-be-called-on-array-instances-sonar-rule-2116) ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))
    * ["Iterator.next()" methods should throw "NoSuchElementException"](#iteratornext-methods-should-throw-nosuchelementexception-sonar-rule-2272) ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))
    * [Strings and Boxed types should be compared using "equals()"](#strings-and-boxed-types-should-be-compared-using-equals-sonar-rule-4973) ([Sonar Rule 4973](https://rules.sonarsource.com/java/RSPEC-4973))
    * [Math operands should be cast before assignment](#math-operands-should-be-cast-before-assignment-sonar-rule-2184) ([Sonar Rule 2184](https://rules.sonarsource.com/java/RSPEC-2184))
    * [JEE applications should not "getClassLoader"](#jee-applications-should-not-getclassloader-sonar-rule-3032) ([Sonar Rule 3032](https://rules.sonarsource.com/java/RSPEC-3032))
    * ["compareTo" should not return "Integer.MIN_VALUE"](#compareto-should-not-return-integermin_value-sonar-rule-2167) ([Sonar Rule 2167](https://rules.sonarsource.com/java/RSPEC-2167))
    * [Math should not be performed on floats](#math-should-not-be-performed-on-floats-sonar-rule-2164) ([Sonar Rule 2164](https://rules.sonarsource.com/java/RSPEC-2164))
    * [Synchronization should not be based on Strings or boxed primitives](#synchronization-should-not-be-based-on-Strings-or-boxed-primitives-sonar-rule-1860) ([Sonar Rule 1860](https://rules.sonarsource.com/java/tag/multi-threading/RSPEC-1860))
    * [".equals()" should not be used to test the values of "Atomic" classes](#equals-should-not-be-used-to-test-the-values-of-atomic-classes-sonar-rule-2204) ([Sonar Rule 2204](https://rules.sonarsource.com/java/RSPEC-2204))
    * ["getClass" should not be used for synchronization](#getclass-should-not-be-used-for-synchronization-sonar-rule-3067) ([Sonar Rule 3067](https://rules.sonarsource.com/java/type/Bug/RSPEC-3067))
    * [Variables should not be self-assigned](#variables-should-not-be-self-assigned-sonar-rule-1656) ([Sonar Rule 1656](https://rules.sonarsource.com/java/type/Bug/RSPEC-1656))
* [Code Smell](#code-smell)
    * [Unused assignments should be removed](#unused-assignments-should-be-removed-sonar-rule-1854) ([Sonar Rule 1854](https://rules.sonarsource.com/java/RSPEC-1854))
    * [Fields in a "Serializable" class should either be transient or serializable](#fields-in-a-serializable-class-should-either-be-transient-or-serializable-sonar-rule-1948) ([Sonar Rule 1948](https://rules.sonarsource.com/java/RSPEC-1948))

### *Bug*

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

------

#### "BigDecimal(double)" should not be used ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))

Any constructor of `BigDecimal` that has a parameter of type `float` or `double` is replaced with an invocation of the `BigDecimal.valueOf(parameter)` method.

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

When the constructor of `BigDecimal` being called has two arguments, being the first one of type `float` or `double`, that argument is changed to `String`.

Example:
```diff
        MathContext mc;
-       BigDecimal bd4 = new BigDecimal(2.0, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
-       BigDecimal bd6 = new BigDecimal(2.0f, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
+       BigDecimal bd4 = new BigDecimal("2.0", mc);
+       BigDecimal bd6 = new BigDecimal("2.0", mc);
```

Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/76) that repairs one BigDecimalDoubleConstructor violation.

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

#### "Iterator.next()" methods should throw "NoSuchElementException" ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))

Any implementation of the `Iterator.next()` method that does not throw `NoSuchElementException` has a code snippet added to its start. The code snippet consists of a call to `hasNext()` and a throw of the error.

Example:
```diff
+import java.util.NoSuchElementException;

public class IteratorNextException implements Iterator {
...
     @Override
-    public String next() { // Noncompliant
+    public String next() {
+        if (!hasNext()) {
+            throw new NoSuchElementException();
+        }
         ...
     }
```

Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/75) that repairs two IteratorNextException violations.

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

#### Math operands should be cast before assignment ([Sonar Rule 2184](https://rules.sonarsource.com/java/RSPEC-2184))

In arithmetic expressions, when the operands are `int` and/or `long`, but the result of the expression is assigned to a `long`, `double`, or `float`, the first operand is casted to the final type before the operation takes place.

Example:
```diff
-    float twoThirds = 2/3; // Noncompliant; int division. Yields 0.0
+    float twoThirds = (float) 2/3;
...
     public long compute(int factor){
-        return factor * 10000; // Noncompliant, won't produce the expected result if factor > 214748
+        return (long) factor * 10000; 
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

#### Math should not be performed on floats ([Sonar Rule 2164](https://rules.sonarsource.com/java/RSPEC-2164))

In arithmetic expressions between two `float`s, both left and right operands are casted to `double`.

Example:
```diff
         float a = 16777216.0f;
         float b = 1.0f;
-        float c = a + b; // Noncompliant, yields 1.6777216E7 not 1.6777217E7
+        float c = (double) a + (double) b;
```

-----

#### ".equals()" should not be used to test the values of "Atomic" classes ([Sonar Rule 2204](https://rules.sonarsource.com/java/RSPEC-2204))

Any equality comparison between `AtomicInteger`, `AtomicLong`, or `AtomicBoolean` objects using the method `equals`, i.e., `obj1.equals(obj2)`, is replaced by a binary operator of the kind equals, where the left and right hand operands are calls to the method `.get()` using both objects.

Example:
```diff
 		AtomicInteger aInt1 = new AtomicInteger(0);
 		AtomicInteger aInt2 = new AtomicInteger(0);
-		isEqual = aInt1.equals(aInt2); // Noncompliant
+		isEqual = aInt1.get() == aInt2.get();
```

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

#### "getClass" should not be used for synchronization ([Sonar Rule 3067](https://rules.sonarsource.com/java/RSPEC-3067))

Any invocation using getClass will be typechecked if the object's invoked by `getClass` is final or an enum. If not, the invocation will be transformed to `.class` instead of `getClass`.

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

#### Variables should not be self-assigned ([Sonar Rule 1656](https://rules.sonarsource.com/java/type/Bug/RSPEC-1656))

Any assignment with identical left and right expressions will be processed. If the identifier being using in the self-assignment exists as both a local variable and a field, then the left expression will be changed by adding this. at the beginning of the expression. In any other case, including cases where there are invocations or access to another class field, such as objectA.b = objectA.b, the assignment will be removed.

In particular, this processor will only process self-assigments contained inside a single class file, meaning cases such as `a.b = a.b` will not be processed, while `this.a.b = this.a.b` will be fixed. The motivation is `a.b = a.b` is uncommon and classpath of `a` might not be present during the processing.

Example:
```diff
class SelfAssignement {
   int a,c = 0;
   int[] b = {0};
   int h = 0;
   int[] g = 0;
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

Check out an accepted PR in [Spoon](https://github.com/INRIA/spoon/pull/2265) that repairs one DeadStore violation.

------

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
