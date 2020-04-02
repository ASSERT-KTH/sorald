## Handled SonarQube rules

Sonarqube-repair can currently repair violations of 7 SonarQube rules of which 5 are labeled as `BUG` and 2 as `Code Smell`.

### *BUG*

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

Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/76) that repairs one BigDecimalDoubleConstructor violation.

-----

#### "HashCode" and "toString" should not be called on array instances ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))

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
...
     @Override
-    public String next(){ // Noncompliant
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
-        return b != a;// Noncompliant
+        return !b.equals(a);
...
-        if(firstName == lastName){// Noncompliant
+        if (firstName.equals(lastName)) {
```

Check out an accepted PR in [Apache Sling Discovery](https://github.com/apache/sling-org-apache-sling-discovery-impl/pull/1) that repairs one CompareStringsBoxedTypesWithEquals violation.

-----

### *Code Smell*

#### Dead Stores should be removed ([Sonar Rule 1854](https://rules.sonarsource.com/java/RSPEC-1854))

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

#### Fields in a "Serializable" class should be serializable ([Sonar Rule 1948](https://rules.sonarsource.com/java/RSPEC-1948))

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
