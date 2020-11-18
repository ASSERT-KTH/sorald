# Sorald [![Travis Build Status](https://travis-ci.com/SpoonLabs/sorald.svg?branch=master)](https://travis-ci.com/SpoonLabs/sorald) [![Code Coverage](https://codecov.io/gh/SpoonLabs/sorald/branch/master/graph/badge.svg)](https://codecov.io/gh/SpoonLabs/sorald)
Sorald is a collection of java code analyses and transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of rules contained in [SonarQube](https://rules.sonarsource.com).
It can currently repair violations of [15+ rules](/docs/HANDLED_RULES.md).

## Getting started

### Prerequisites 

A JDK (Java 11+)

### Build

1) Clone this repository: `git clone https://github.com/SpoonLabs/sorald.git`

2) Build:

 ```bash
$ cd sorald
$ mvn package -DskipTests
 ```

The Sorald application can now be found in
`target/sorald-1.1-SNAPSHOT-jar-with-dependencies.jar`.

### Usage

Sorald can do two things: automatically repair violations of Sonar rules in a
project, or mine projects for rule violations. These two modes of operations
are available as the two commands `repair` and `mine`, respectively.

For the remainder of this section, assume that we have defined the following
alias:

```bash
alias sorald='java -jar /abs/path/to/target/sorald-1.1-SNAPSHOT-jar-with-dependencies.jar'
```

#### Repairing rule violations (the `repair` command)

To repair rule violations, use the `repair` command. Assuming that the Sorald
jar-file pointed out in [the build instructions](#build) is located at
`sorald.jar`, the command can be invoked like so.

```bash
$ sorald repair <arguments ...>
```

Basic usage consists of specifying a project to repair, as well as one or more
rules to target. The available rules [can be found here](docs/HANDLED_RULES.md),
and are specified by their key. For example, to repair violations of the rules
with keys 2111, 2184 and 2204 in a project located at `some/project/path`, one
can invoke Sorald like so.

```bash
$ sorald repair --originalFilesPath some/project/path --ruleKeys 2111,2184,2204
```

The full list of options is as follows (and can also be found by running
`sorald repair --help`):

```bash
      --fileOutputStrategy=<fileOutputStrategy>
                  Mode for outputting files: 'CHANGED_ONLY', which means that
                    only changed files will be created in the workspace, and
                    'ALL', which means that all files, including the unchanged
                    ones, will be created in the workspace.
      --gitRepoPath=<gitRepoPath>
                  The path to a git repository directory.
  -h, --help      Show this help message and exit.
      --maxFilesPerSegment=<maxFilesPerSegment>
                  Max number of files per loaded segment for segmented repair.
                    It should be >= 3000 files per segment.
      --maxFixesPerRule=<maxFixesPerRule>
                  Max number of fixes per rule.
      --originalFilesPath=<originalFilesPath>
                  The path to the file or folder to be analyzed and possibly
                    repaired.
      --prettyPrintingStrategy=<prettyPrintingStrategy>
                  Mode for pretty printing the source code: 'NORMAL', which
                    means that all source code will be printed and its
                    formatting might change (such as indentation), and
                    'SNIPER', which means that only statements changed towards
                    the repair of Sonar rule violations will be printed.
      --repairStrategy=<repairStrategy>
                  Type of repair strategy. DEFAULT - load everything without
                    splitting up the folder in segments, SEGMENT - splitting
                    the folder into smaller segments and repair one segment at
                    a time (need to specify --maxFilesPerSegment if not default)
      --ruleKeys=<ruleKeys>[,<ruleKeys>...]
                  Choose one or more of the following rule keys (use ',' to
                    separate multiple keys):
                  1656: Variables should not be self-assigned
                  2142: "InterruptedException" should not be ignored
                  2111: "BigDecimal(double)" should not be used
                  4973: Strings and Boxed types should be compared using
                    "equals()"
                  2204: ".equals()" should not be used to test the values of
                    "Atomic" classes
                  1854: Unused assignments should be removed
                  1444: "public static" fields should be constant
                  	(incomplete: does not fix variable naming)
                  2184: Math operands should be cast before assignment
                  2164: Math should not be performed on floats
                  1860: Synchronization should not be based on Strings or boxed
                    primitives
                  3032: JEE applications should not "getClassLoader"
                  1948: Fields in a "Serializable" class should either be
                    transient or serializable
                  2272: "Iterator.next()" methods should throw
                    "NoSuchElementException"
                  3984: Exception should not be created without being thrown
                  3067: "getClass" should not be used for synchronization
                  2167: "compareTo" should not return "Integer.MIN_VALUE"
                  2116: "hashCode" and "toString" should not be called on array
                    instances
                  2095: Resources should be closed
                  2755: XML parsers should not be vulnerable to XXE attacks
                  	(incomplete: This processor is a WIP and currently supports
                    a subset of rule 2755. See Sorald's documentation for
                    details.)
  -V, --version   Print version information and exit.
      --workspace=<soraldWorkspace>
                  The path to a folder that will be used as workspace by
                    Sorald, i.e. the path for the output.
```

> **Note:** Some rules (e.g. 1444) are marked as "incomplete". This means that
> Sorald's repair for a violation of said rule is either partial or
> situational.

#### Mining Sonar warnings (the `mine` command)

To mine projects for Sonar warnings, use the `mine` command. Its most basic
usage consists of simply pointing it to a project directory.

```bash
$ sorald mine --originalFilesPath path/to/project
```

It will then output statistics for that project with the Sonar checks available
in Sorald.

Another option is to execute the miner on a list of remote Git repositories,
which can be done like so.

```bash
$ sorald mine --statsOnGitRepos --gitReposList repos.txt --statsOutputFile output.txt --tempDir /tmp
```

The `--gitReposList` should be a plain text file with one remote repository url
(e.g. `https://github.com/SpoonLabs/sorald.git`) per line. Sorald clones each
repository and runs Sonar checks on the head of the default branch.

The full list of options is as follows (and can also be found by running `sorald
mine --help`).

```bash
Mine a project for Sonar warnings.
      --gitReposList=<reposList>
                            The path to the repos list.
  -h, --help                Show this help message and exit.
      --originalFilesPath=<originalFilesPath>
                            The path to the file or folder to be analyzed and
                              possibly repaired.
      --ruleTypes=<ruleTypes>[,<ruleTypes>...]
                            One or more types of rules to check for (use ',' to
                              separate multiple types). Choices: BUG,
                              VULNERABILITY, CODE_SMELL, SECURITY_HOTSPOT
      --statsOnGitRepos     If the stats should be computed on git repos.
      --statsOutputFile=<statsOutputFile>
                            The path to the output file.
      --tempDir=<tempDir>   The path to the temp directory.
  -V, --version             Print version information and exit.
```

#### Running Sorald on GitHub projects to propose PRs with fixes

To run Sorald on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sorald is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Academic bibliographic references

"[A template-based approach to automatic program repair of Sonarqube static warnings](http://kth.diva-portal.org/smash/get/diva2:1433710/FULLTEXT01.pdf)", by Haris Adzemovic, Master's thesis, KTH, School of Electrical Engineering and Computer Science (EECS), 2020. [(bibtex)](http://www.diva-portal.org/smash/references?referenceFormat=BIBTEX&pids=[diva2:1433710]&fileName=export.txt)
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
