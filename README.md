# Sonarqube-repair

Sonarqube-repair is a collection of java code transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of static rules defined by [Sonarqube](https://rules.sonarsource.com).

## Handled Sonarqube rules
Sonarqube-repair can currently repair violations of 7 Sonarqube rules of which 5 are labeled as `BUG` and 2 as `Code Smell`.
##### NOTE:
Both processors for code smells and one bug (Resources should be closed) are written in a different way from the others, using the plugin Sonarjava for localising bugs. The processors and their implementations have not been maintained and your milage may vary.
The remaining processors leverage the capabilities of Spoon for both localising and repairing violations.

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

#### Strings and Boxed types should be compared using "equals()" ([Sonar Rule 2272](https://rules.sonarsource.com/java/RSPEC-4973))
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
------

## Getting Started
### Prerequisites 
A JDK (java 1.8 or above) 

### Usage
If you wish to run Sonarqube-repair on your own project, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for en example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.

##### If you want to run it from your IDE

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

Import it as maven project in your favorite Java IDE.
Create the folder `sonarqube-repair/source/act/`
Put files to be repaired in the new folder.
Run the main function. 

You can either give command line arguments or set the rule number and project-key manually in the main function in `src/main/java/Main.java`. The repaired files will appear in `sonarqube-repair/spooned/` . Also you need to set the url of the sonar analysis of your project in `src/main/java/ParseAPI.java` if it is different from [sonarcloud.io](https://sonarcloud.io/about)
 
## Running the tests

run `mvn test` to run the tests for all repairs.

## Contributing

### Issues 
Feel free to open issues on this github repository.

### Pull requests
Pull requests for improvements or new features are welcome by all contributers.

## Authors
* Ashutosh Kumar Verma [ashutosh1598](https://github.com/ashutosh1598)
* Martin Monperrus [monperrus](https://github.com/monperrus)
* Pavel Pvojtechovsky  [pvojtechovsky](https://github.com/pvojtechovsky)
* Haris Adzemovic [HarisAdzemovic](https://github.com/HarisAdzemovic)

## Implementation notes
#### This only applies to the 2 Code Smells and Bug S2095
Sonarjava had to be changed in order to return the appropriate issue information for offline-repair. The changes are at https://github.com/kth-tcs/sonar-java/pull/1 . Now I(Ashutosh) don't think that doing offline-repair is a good idea and we should stick to using the web api. I don't know of a way to offline detect issues which have their components in more than one file.

## Case studies

Spoon SonarQube: <https://sonarqube.ow2.org/project/issues?id=fr.inria.gforge.spoon%3Aspoon-core&resolved=false&types=BUG>
