## Handled Sonarqube rules

Sonarqube-repair can currently repair violations of 7 Sonarqube rules of which 5 are labeled as `BUG` and 2 as `Code Smell`.

### *BUG*

#### Resources should be closed ([Sonar Rule 2095](https://rules.sonarsource.com/java/RSPEC-2095))

The repair encloses the parent block of resource intialization in a try-with resources.
If it was already in a try block it replaces the try with try-with-resources instead 
of creating a new one, so that useless nested try blocks are not created.

[ResourceCloseProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/ResourceCloseProcessor.java)

------
#### "BigDecimal(double)" should not be used ([Sonar Rule 2111](https://rules.sonarsource.com/java/RSPEC-2111))
Any constructor of BigDecimal which has a parameter of type `float` or `double` is replaced with an invocation of the BigDecmail.valueOf(parameter) method.

Pull Requests:

* [Apache PDFBox](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/pdfbox/2111)
* [Apache Commons Configuration](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/commons-configuration/2111)
-----
#### "HashCode" and "toString" should not be called on array instances ([Sonar Rule 2116](https://rules.sonarsource.com/java/RSPEC-2116))
Any invocation of toString() or hashCode() on an array is replaced with Arrays.toString(parameter) or Arrays.hashCode(parameter).

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/spoon-core/2116)

-----
#### "Iterator.next()" methods should throw "NoSuchElementException" ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-2272))
Any implementation of the Itarator next() method which does not throw the NoSuchElementException has a code snippet added to its start. The code snippet consists of a call to hasNext() and a throw of the error.

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/spoon-core/2272)
* [Apache PDFBox](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/pdfbox/2272)
-----

#### Strings and Boxed types should be compared using "equals()" ([Sonar Rule 4973](https://rules.sonarsource.com/java/RSPEC-4973))
Any comparison of strings or boxed types using `==` or `!=` is replaced by `equals`

Pull Requests:

* [Spoon](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/spoon-core/4973)
* [Apache JSPWiki](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/jspwiki/4973)
* [Apache Sling Auth Core](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-auth-core/4973)
* [Apache Sling Discovery](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-discovery/4973)
* [Apache Sling Feature](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-feature/4973)
* [Apache Sling Launchpad Base](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-launchpad-base/4973)
* [Apache Sling Scripting ESX](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-scripting-esx/4973)
* [Apache Sling Scripting JCR](https://github.com/kth-tcs/sonarqube-repair/tree/master/pull-requests/sling-scripting-jcr/4973)
-----

### *Code Smell*

#### Dead Stores should be removed ([Sonar Rule 1854](https://rules.sonarsource.com/java/RSPEC-1854))

The repair consists of deleting useless assignments.

[DeadStoreProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/DeadStoreProcessor.java)

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

[SerializableFieldProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/SerializableFieldProcessor.java)

Merged Pull Requests:

* https://github.com/INRIA/spoon/pull/2059  (removes 10 sonarqube bugs)
* https://github.com/INRIA/spoon/pull/2121  (removes 3 sonarqube bugs)
* https://github.com/INRIA/spoon/pull/2241  (removes 83 sonarqube bugs)

##### NOTE:

Both processors for code smells and one bug (Resources should be closed) are written in a different way from the others, using the plugin Sonarjava for localising bugs. The processors and their implementations have not been maintained and your milage may vary.
The remaining processors leverage the capabilities of Spoon for both localising and repairing violations.
