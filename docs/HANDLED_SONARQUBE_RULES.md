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
             zipInput.close();
-            } catch (Exception e) {
-                Launcher.LOGGER.error(e.getMessage(), e);
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

Pull Requests:

* [Apache PDFBox](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/pdfbox/2111)
* [Apache Commons Configuration](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/commons-configuration/2111)

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

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/spoon-core/2116)

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

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/spoon-core/2272)
* [Apache PDFBox](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/pdfbox/2272)

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

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/spoon-core/4973)
* [Apache JSPWiki](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/jspwiki/4973)
* [Apache Sling Auth Core](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-auth-core/4973)
* [Apache Sling Discovery](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-discovery/4973)
* [Apache Sling Feature](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-feature/4973)
* [Apache Sling Launchpad Base](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-launchpad-base/4973)
* [Apache Sling Scripting ESX](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-scripting-esx/4973)
* [Apache Sling Scripting JCR](https://github.com/kth-tcs/sonarqube-repair/tree/master/experimentation/pull-requests/sling-scripting-jcr/4973)

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

Merged Pull Requests:

* https://github.com/INRIA/spoon/pull/2265
(removes one sonar violation)
* https://github.com/INRIA/spoon/pull/2256
(removes two sonar violations)

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

Merged Pull Requests:

* https://github.com/INRIA/spoon/pull/2059  (removes 10 SonarQube bugs)
* https://github.com/INRIA/spoon/pull/2121  (removes 3 SonarQube bugs)
* https://github.com/INRIA/spoon/pull/2241  (removes 83 SonarQube bugs)
