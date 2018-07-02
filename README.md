# sonarqube-repair

This is the initial draft of the system to repair sonarqube bugs.

**List of Merged Pull Requests :**

1- https://github.com/INRIA/spoon/pull/2059

2- https://github.com/INRIA/spoon/pull/2121

**List of handled Sonarqube bugs:**

1- Null pointers should not be dereferenced(Sonar Rule 2259) -   
The repair is implemented as follows :

Consider a statement like `x.functionCall();` where `x` is nullable. If `x` is
a variable, then the repair adds an if-check to test if `x` is null.
If `x` is itself the result of a function call, it is enclosed by a try-catch
block. The repair does not yet handle array dereference like `arr[0]` where `arr`
is nullable.

2- Dead Stores should be removed(Sonar Rule 1854) -   
A simple repair, it just deletes the useless assignments.

3- Fields in a "Serializable" class should be serializable(Sonar rule 1948) -
Currently the repair just adds the modifier transient to all non-serializable
fields, but the plan is to give user the option if they want to go the class
of that field and add `implements Serializable` to it.

4- non-serializable super class of a "Serializable" class should have a
 no-argument constructor(Sonar Rule 2055). -    
 The repair adds a no-argument empty constructor to the superclass.
 
5- Resources should be closed(Sonar Rule 2095) -   

The repair encloses the parent block of resource intialization in a try-with resources.
If it was already in a try block it replaces the try with try-with-resources instead 
of creating a new one, so that useless nested try blocks are not created.

 


**Usage:**

To use it, run:

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

`cd sonarqube-repair`

Put the source files you want to repair
in the directory "sonarqube-repair/source/act/"

Then do :

`mvn -U clean package`

If you want to skip tests, run : 
 
`mvn -U clean package -Dmaven.test.skip=true`

`java -jar target/sonarqube-repair-0.1-SNAPSHOT-jar-with-dependencies.jar arg1 arg2`

If this command is run from somewhere other than `sonarqube-repair/` , you need
 to create a directory `source/act/` in that location and put your source 
 files there.


arg1 and arg2 are optional parameters.
Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you 
can get from https://rules.sonarsource.com/java/type/Bug .
Second argument is the projectKey for the sonarqube analysis of source files. for 
example "fr.inria.gforge.spoon:spoon-core"
If you don't provide arguments, default value will be used.

The repaired code will appear in ./spooned/ from where the command is run.

Feel free to open issues.
