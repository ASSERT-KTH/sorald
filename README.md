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


### Non-serializable super class of a "Serializable" class should have a no-argument constructor ([Sonar Rule 2055](https://rules.sonarsource.com/c/RSPEC-2055)) -

The repair adds a no-argument empty constructor to the superclass.

[DeadStoreProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/DeadStoreProcessor.java)

### Resources should be closed ([Sonar Rule 2095](https://rules.sonarsource.com/c/RSPEC-2095)) -

The repair encloses the parent block of resource intialization in a try-with resources.
If it was already in a try block it replaces the try with try-with-resources instead 
of creating a new one, so that useless nested try blocks are not created.

[ResourceCloseProcessor](https://github.com/kth-tcs/sonarqube-repair/blob/master/src/main/java/ResourceCloseProcessor.java)

## Usage

To use it, run:

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

`cd sonarqube-repair`

`mvn -U clean package`

To start sonarqube-repair:

`java -jar target/sonarqube-repair-0.1-SNAPSHOT-jar-with-dependencies.jar arg1 arg2`

arg1 and arg2 are optional parameters.
Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you 
can get from https://rules.sonarsource.com/java/type/Bug .
Second argument is the projectKey for the sonarqube analysis of source files. for 
example "fr.inria.gforge.spoon:spoon-core"
If you don't provide arguments, default value will be used.

The repaired code will appear in ./spooned/ from where the command is run.

If this command is run from somewhere other than `sonarqube-repair/` , you need
 to create a directory `source/act/` in that location and put your source 
 files there.

## Case studies

Spoon SonarQube: <https://sonarqube.ow2.org/project/issues?id=fr.inria.gforge.spoon%3Aspoon-core&resolved=false&types=BUG>


