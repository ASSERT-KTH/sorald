# Sorald [![Travis Build Status](https://travis-ci.com/SpoonLabs/sorald.svg?branch=master)](https://travis-ci.com/SpoonLabs/sorald)

Sorald is a collection of java code analyses and transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of rules contained in [SonarQube](https://rules.sonarsource.com).
It can currently repair violations of [15+ rules](/docs/HANDLED_RULES.md).

## Getting started

### Prerequisites 

A JDK (java 1.8)

### Usage

##### If you want to run it from source code

1) Clone this repository: `git clone https://github.com/SpoonLabs/sorald.git`

2) Build and run the tool:

 ```bash
$ cd sorald
$ mvn package -DskipTests
$ java -jar target/sorald-1.1-SNAPSHOT-jar-with-dependencies.jar <arguments>
 ```

The arguments are the following:

```bash
    [--ruleKeys ruleKeys1,ruleKeys2,...,ruleKeysN ]
        Choose one or more of the following rule keys (use ',' to separate
        multiple keys):
        2272: "Iterator.next()" methods should throw "NoSuchElementException"
        1860: Synchronization should not be based on Strings or boxed primitives
        2116: "hashCode" and "toString" should not be called on array instances
        2184: Math operands should be cast before assignment
        4973: Strings and Boxed types should be compared using "equals()"
        2095: Resources should be closed
        3984: Exception should not be created without being thrown
        2164: Math should not be performed on floats
        2167: "compareTo" should not return "Integer.MIN_VALUE"
        3032: JEE applications should not "getClassLoader"
        1656: Variables should not be self-assigned
        3067: "getClass" should not be used for synchronization
        2204: ".equals()" should not be used to test the values of "Atomic"
        classes
        1948: Fields in a "Serializable" class should either be transient or
        serializable
        2142: "InterruptedException" should not be ignored
        1854: Unused assignments should be removed
        2111: "BigDecimal(double)" should not be used

  --originalFilesPath <originalFilesPath>
        The path to the file or folder to be analyzed and possibly repaired.

  [--workspace <workspace>]
        The path to a folder that will be used as workspace by Sorald, i.e. the
        path for the output. (default: ./sorald-workspace)

  [--gitRepoPath <gitRepoPath>]
        The path to a git repository directory.

  [--prettyPrintingStrategy <prettyPrintingStrategy>]
        Mode for pretty printing the source code: 'NORMAL', which means that all
        source code will be printed and its formatting might change (such as
        indentation), and 'SNIPER', which means that only statements changed
        towards the repair of Sonar rule violations will be printed. (default:
        SNIPER)

  [--fileOutputStrategy <fileOutputStrategy>]
        Mode for outputting files: 'CHANGED_ONLY', which means that only changed
        files will be created in the workspace, and 'ALL', which means that all
        files, including the unchanged ones, will be created in the workspace.
        (default: CHANGED_ONLY)

  [--maxFixesPerRule <maxFixesPerRule>]
        Max number of fixes per rule. Default: Integer.MAX_VALUE (or all)
        (default: 2147483647)

  [--maxFilesPerSegment <maxFilesPerSegment>]
        Max number of files per loaded segment for segmented repair. It should
        be >= 3000 files per segment. Default: 6500 (256mb Jvm) .  (default:
        6500)

  [--repairStrategy <repairStrategy>]
        Type of repair strategy. DEFAULT - load everything without splitting up
        the folder in segments, SEGMENT - splitting the folder into smaller
        segments and repair one segment at a time (need to specify
        --maxFilesPerSegment if not default). Default: DEFAULT (default:
        DEFAULT)

  [-h|--help]
```

Example of a concrete call to Sorald, in which multiple rule keys are given as input:

```bash
$ java -jar target/sorald-1.1-SNAPSHOT-jar-with-dependencies.jar --originalFilesPath src/test/resources/MultipleProcessors.java --workspace /tmp/ --ruleKeys 2111,2184,2204
```
 
##### If you want to run it on GitHub projects to propose PRs with fixes

To run Sorald on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sorald is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
