# Sonarqube-repair [![Travis Build Status](https://travis-ci.com/SpoonLabs/sonarqube-repair.svg?branch=master)](https://travis-ci.com/SpoonLabs/sonarqube-repair)

Sonarqube-repair is a collection of java code analyses and transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of rules contained in [SonarQube](https://rules.sonarsource.com).
It can currently repair violations of [15+ rules](/docs/HANDLED_RULES.md).

## Getting started

### Prerequisites 

A JDK (java 1.8)

### Usage

##### If you want to run it from source code

1) Clone this repository: `git clone https://github.com/SpoonLabs/sonarqube-repair.git`

2) Build and run the tool:

 ```bash
$ cd sonarqube-repair
$ mvn package -DskipTests
$ java -jar target/sonarqube-repair-1.1-SNAPSHOT-jar-with-dependencies.jar <arguments>
 ```

The arguments are the following:

```bash
  [--ruleKeys <ruleKeys>]
        Choose one of the following rule keys:
        2272: "Iterator.next()" methods should throw "NoSuchElementException"
        2116: "hashCode" and "toString" should not be called on array instances
        1860: Synchronization should not be based on Strings or boxed primitives
        2184: Math operands should be cast before assignment
        4973: Strings and Boxed types should be compared using "equals()"
        2095: Resources should be closed
        3984: Exception should not be created without being thrown
        2164: Math should not be performed on floats
        2167: "compareTo" should not return "Integer.MIN_VALUE"
        3032: JEE applications should not "getClassLoader"
        1656: Variables should not be self-assigned
        3067: "getClass" should not be used for synchronization
        1948: Fields in a "Serializable" class should either be transient or
        serializable
        2204: ".equals()" should not be used to test the values of "Atomic"
        classes
        1854: Unused assignments should be removed
        2111: "BigDecimal(double)" should not be used (default: 2116)

  --originalFilesPath <originalFilesPath>
        The path to the file or folder to be analyzed and possibly repaired.

  [--workspace <workspace>]
        The path to a folder that will be used as workspace by sonarqube-repair,
        i.e. the path for the output. (default: ./sonar-workspace)

  [--gitRepoPath <gitRepoPath>]
        The path to a git repository directory.

  [--prettyPrintingStrategy <prettyPrintingStrategy>]
        Mode for pretty printing the source code: 'NORMAL', which means that all
        source code will be printed and its formatting might change (such as
        indentation), and 'SNIPER', which means that only statements changed
        towards the repair of sonar rule violations will be printed. (default:
        NORMAL)

  [--fileOutputStrategy <fileOutputStrategy>]
        Mode for outputting files: 'CHANGED_ONLY', which means that only changed
        files will be created in the workspace, and 'ALL', which means that all
        files, including the unchanged ones, will be created in the workspace.
        (default: CHANGED_ONLY)

  [-h|--help]
```

Example of a concrete call to sonarqube-repair, in which multiple rule keys are given as input:

```bash
$ java -jar target/sonarqube-repair-1.1-SNAPSHOT-jar-with-dependencies.jar --originalFilesPath src/test/resources/MultipleProcessors.java --workspace /tmp/ --ruleKeys 2111,2184,2204
```
 
##### If you want to run it on GitHub projects to propose PRs with fixes

To run Sonarqube-repair on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
