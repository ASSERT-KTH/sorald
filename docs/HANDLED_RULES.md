## Handled rules

Sonarqube-repair can currently repair violations of 8 rules of which 6 are labeled as `BUG` and 2 as `Code Smell`:

* [Bug](#bug)
    * [Resources should be closed](#resources-should-be-closed-sonar-rule-2095) ([Sonar Rule 2095](https://rules.sonarsource.com/java/RSPEC-2095))
    * ["BigDecimal(double)" should not be used](#bigdecimaldouble-should-not-be-used-sonar-rule-2111) ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))
    * ["hashCode" and "toString" should not be called on array instances](#hashcode-and-tostring-should-not-be-called-on-array-instances-sonar-rule-2116) ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))
    * ["Iterator.next()" methods should throw "NoSuchElementException"](#iteratornext-methods-should-throw-nosuchelementexception-sonar-rule-2272) ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))
    * [Strings and Boxed types should be compared using "equals()"](#strings-and-boxed-types-should-be-compared-using-equals-sonar-rule-4973) ([Sonar Rule 4973](https://rules.sonarsource.com/java/RSPEC-4973))
    * [Math operands should be cast before assignment](#math-operands-should-be-cast-before-assignment-sonar-rule-2184) ([Sonar Rule 2184](https://rules.sonarsource.com/java/RSPEC-2184))
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

Using the standard `getClassLoader()` may not return the right class loader in Java Enterprise Edition context. Instead, `getClassLoader()` usage such as `this.getClass().getClassLoader()` and `Dummy.class.getClassLoader()` should be replaced by `Thread.currentThread().getClassLoader()`. In particular, such replacement only occurs if the `.java` files use the `javax` package in its imports.

Example:
```diff
+    ClassLoader d = Thread.currentThread().getContextClassLoader();
-    ClassLoader d = this.getClass().getClassLoader(); // Noncompliant
+    public void case1() {
+        ClassLoader c = Thread.currentThread().getContextClassLoader();
+
+    }
-    public void case1() {
-        ClassLoader c = this.getClass().getClassLoader(); // Noncompliant
-    }
+    public void case2() throws ClassNotFoundException {
+        Thread.currentThread().getContextClassLoader().loadClass("anotherclass");
+    }
-    public void case2() throws ClassNotFoundException{
-        Dummy.class.getClassLoader().loadClass("anotherclass"); // Noncompliant
-    }
+    abstract class InnerClass {
+        ClassLoader f = Thread.currentThread().getContextClassLoader();
+        public void hello() {
+            Thread.currentThread().getContextClassLoader();
+        }
+    }
-    abstract class InnerClass {
-        ClassLoader f = this.getClass().getClassLoader(); // Noncompliant 
-        public void hello() {
-            this.getClass().getClassLoader(); // Noncompliant
-       }
-    }
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
