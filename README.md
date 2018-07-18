# sonarqube-repair

sonarqube-repair is a system to repair sonarqube bugs.

## Handled Sonarqube bugs

### Null pointers should not be dereferenced ([Sonar Rule 2259](https://rules.sonarsource.com/c/RSPEC-2259)) -   

The repair is implemented as follows :

Consider a statement like `x.functionCall();` where `x` is nullable. If `x` is
a variable, then the repair adds an if-check to test if `x` is null.
If `x` is itself the result of a function call, it is enclosed by a try-catch
block. The repair does not handle array dereference like `arr[0]` where `arr`
is nullable.

[NullDereferenceProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/NullDereferenceProcessor.java)

### Dead Stores should be removed([Sonar Rule 1854](https://rules.sonarsource.com/c/RSPEC-1854)) -   

The repair repair consists of deleting the useless assignment.

[DeadStoreProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/DeadStoreProcessor.java)

### Fields in a "Serializable" class should be serializable ([Sonar Rule 1948](https://rules.sonarsource.com/c/RSPEC-1948)) -

The repair adds the modifier `transient` to all non-serializable
fields. In the future, the plan is to give user the option if they want to go the class
of that field and add `implements Serializable` to it.

[SerializableFieldProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/SerializableFieldProcessor.java)

Merged Pull Requests:

* https://github.com/INRIA/spoon/pull/2059
* https://github.com/INRIA/spoon/pull/2121
* https://github.com/INRIA/spoon/pull/2241  (very successful, removes 83 sonarqube bugs in one go)


### Non-serializable super class of a "Serializable" class should have a no-argument constructor ([Sonar Rule 2055](https://rules.sonarsource.com/c/RSPEC-2055)) -

The repair adds a no-argument empty constructor to the superclass, or removes
"implements Serializable" from the subclass, depending on user's choice.

Merged Pull Requests:

* https://github.com/INRIA/spoon/pull/2173

[DeadStoreProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/DeadStoreProcessor.java)

### Resources should be closed ([Sonar Rule 2095](https://rules.sonarsource.com/c/RSPEC-2095)) -

The repair encloses the parent block of resource intialization in a try-with resources.
If it was already in a try block it replaces the try with try-with-resources instead 
of creating a new one, so that useless nested try blocks are not created.

[ResourceCloseProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/ResourceCloseProcessor.java)

## Getting Started

### Prerequisites 
A JDK(java 1.8 or above) and a java IDE is needed to use this repair system.
Maven is required to build the project.

### Usage
To use it, run:

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

Import it as maven project in your favourtite Java IDE. Put files to be 
repaired in `sonarqube-repair/source/act/`. Note that you need to create this directory.
Then run the main function. You can either give command line arguments or set the rule
number and project-key manually in the main function in `src/main/java/Main.java`.
 The repaired files will
appear in `sonarqube-repair/spooned/` . Also you need to set the url of
 the sonar analysis of your project in `src/main/java/ParseAPI.java`
  if it is different from [sonarcloud.io](https://sonarcloud.io/about)
 
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

## Implementation notes

Sonarjava had to be changed in order to return the appropriate issue information for offline-repair. The changes are at https://github.com/kth-tcs/sonar-java/pull/1 . Now I(Ashutosh) don't think that doing offline-repair is a good idea and we should stick to using the web api. I don't know of a way to offline detect issues which have their components in more than one file.

## Case studies

Spoon SonarQube: <https://sonarqube.ow2.org/project/issues?id=fr.inria.gforge.spoon%3Aspoon-core&resolved=false&types=BUG>


