# Sorald [![Travis Build Status](https://travis-ci.com/SpoonLabs/sorald.svg?branch=master)](https://travis-ci.com/SpoonLabs/sorald) [![Code Coverage](https://codecov.io/gh/SpoonLabs/sorald/branch/master/graph/badge.svg)](https://codecov.io/gh/SpoonLabs/sorald)
Sorald is a tool to automatically repair violations of static analysis rules checked with [SonarQube](https://rules.sonarsource.com).
It can currently repair violations of [15+ rules](/docs/HANDLED_RULES.md) based on the design described [Sorald: Automatic Patch Suggestions for SonarQube Static Analysis Violations](http://arxiv.org/pdf/2103.12033).

If you use Sorald in an academic context, please cite:

```
@techreport{arXiv-2103.12033,
 title = {Sorald: Automatic Patch Suggestions for SonarQube Static Analysis Violations},
 year = {2021},
 author = {Khashayar Etemadi and Nicolas Harrand and Simon Larsen and Haris Adzemovic and Henry Luong Phu and Ashutosh Verma and Fernanda Madeiral and Douglas Wikstrom and Martin Monperrus},
 url = {http://arxiv.org/pdf/2103.12033},
 number = {2103.12033},
 institution = {arXiv},
}
```

## Getting started

### Prerequisites 

For running Sorald, all you need is a Java 11+ runtime.

For building Sorald from source, you need a Java 11+ JDK, Maven and Git.

### Getting a Sorald JAR

To run Sorald, you need to first get your hands on the program. You can do this
either by [building from source](#build), or going to the [latest
release](https://github.com/spoonlabs/sorald/releases/tag/sorald-0.1.0) and
downloading the file called `sorald-<VERSION>-jar-with-dependencies.jar` listed
under `Assets`. Unless you keep multiple versions of Sorald, we recommend
renaming the JAR to `sorald.jar` for the sake of simplicity.

### Build

1. Clone this repository: `git clone https://github.com/SpoonLabs/sorald.git`

2. Build:

 ```bash
$ cd sorald
$ mvn package -DskipTests
$ cp target/sorald-*-jar-with-dependencies.jar sorald.jar
 ```

The Sorald application can now be found in `sorald.jar` in the current working
directory.

### Usage

Sorald can perform two different tasks: automatically repair violations of Sonar rules in a
project, or mine projects for rule violations. These two modes of operations
are available as the two commands `repair` and `mine`, respectively.

For the remainder of this section, assume that we have defined the following
alias:

```bash
alias sorald='java -jar /abs/path/to/sorald.jar'
```

If you don't like using aliases, simply substitute in `java -jar sorald.jar`
for any occurence of `sorald` in these instructions.

#### Repairing rule violations (the `repair` command)

To repair rule violations, use the `repair` command.

```bash
$ sorald repair <arguments ...>
```

Basic usage consists of specifying a project to target and a rule to repair
violations of. The available rules [can be found here](docs/HANDLED_RULES.md),
and are specified by their key. For example, to repair violations of the rule
`2111: "BigDecimal(double)" should not be used` in a project at
`some/project/path`, one can invoke Sorald like so.

```bash
$ sorald repair --source some/project/path --rule-key 2111
```

The full list of options is as follows (and can also be found by running
`sorald repair --help`):

```bash
Repair Sonar rule violations in a targeted project.
  -h, --help                 Show this help message and exit.
      --max-files-per-segment=<maxFilesPerSegment>
                             Max number of files per loaded segment for
                               segmented repair. It should be >= 3000 files per
                               segment.
      --max-fixes-per-rule=<maxFixesPerRule>
                             Max number of fixes per rule.
      --source=<source>
                             The path to the file or folder to be analyzed and
                               possibly repaired.
      --pretty-printing-strategy=<prettyPrintingStrategy>
                             Mode for pretty printing the source code:
                               'NORMAL', which means that all source code will
                               be printed and its formatting might change (such
                               as indentation), and 'SNIPER', which means that
                               only statements changed towards the repair of
                               Sonar rule violations will be printed.
      --repair-strategy=<repairStrategy>
                             Type of repair strategy. DEFAULT - load everything
                               without splitting up the folder in segments,
                               MAVEN - use Maven to locate production source
                               code and the classpath (test source code is
                               ignored), SEGMENT - splitting the folder into
                               smaller segments and repair one segment at a
                               time (need to specify --maxFilesPerSegment if
                               not default)
      --rule-key=<ruleKey>   Choose one of the following rule keys:
                             1217: "Thread.run()" should not be called directly
                             1444: "public static" fields should be constant
                             	(incomplete: does not fix variable naming)
                             1656: Variables should not be self-assigned
                             1854: Unused assignments should be removed
                             1860: Synchronization should not be based on
                               Strings or boxed primitives
                             1948: Fields in a "Serializable" class should
                               either be transient or serializable
                             2057: Every class implementing Serializable should
                               declare a static final serialVersionUID.
                                (incomplete: This processor does not address the
                               case where the class already has a
                               serialVersionUID with a non long type.)
                             2095: Resources should be closed
                             2111: "BigDecimal(double)" should not be used
                             2116: "hashCode" and "toString" should not be
                               called on array instances
                             2142: "InterruptedException" should not be ignored
                             2164: Math should not be performed on floats
                             2167: "compareTo" should not return "Integer.
                               MIN_VALUE"
                             2184: Math operands should be cast before
                               assignment
                             2204: ".equals()" should not be used to test the
                               values of "Atomic" classes
                             2225: "toString()" and "clone()" methods should
                               not return null
                             	(incomplete: does not fix null returning clone())
                             2272: "Iterator.next()" methods should throw
                               "NoSuchElementException"
                             2755: XML parsers should not be vulnerable to XXE
                               attacks
                             	(incomplete: This processor is a WIP and
                               currently supports a subset of rule 2755. See
                               Sorald's documentation for details.)
                             3032: JEE applications should not "getClassLoader"
                             3067: "getClass" should not be used for
                               synchronization
                             3984: Exception should not be created without
                               being thrown
                             4973: Strings and Boxed types should be compared
                               using "equals()"
      --stats-output-file=<statsOutputFile>
                             Path to a file to store execution statistics in
                               (in JSON format). If left unspecified, Sorald
                               does not gather statistics.
      --target=<target>      The target of this execution (ex. sorald/92d377).
                               This will be included in the json report.
  -V, --version              Print version information and exit.
      --violation-specs=<ruleViolationSpecifiers>[,<ruleViolationSpecifiers>...]
                             One or more rule violation specifiers. Specifiers
                               can be gathered with the 'mine' command using
                               the --stats-output-file option.
```

> **Note:** Some rules (e.g. 1444) are marked as "incomplete". This means that
> Sorald's repair for a violation of said rule is either partial or
> situational.

#### Mining Sonar warnings (the `mine` command)

To mine projects for Sonar warnings, use the `mine` command. Its most basic
usage consists of simply pointing it to a project directory.

```bash
$ sorald mine --source path/to/project
```

It will then output statistics for that project with the Sonar checks available
in Sorald.

Another option is to execute the miner on a list of remote Git repositories,
which can be done like so.

```bash
$ sorald mine --stats-on-git-repos --git-repos-list repos.txt --stats-output-file output.txt --temp-dir /tmp
```

The `--gitReposList` should be a plain text file with one remote repository url
(e.g. `https://github.com/SpoonLabs/sorald.git`) per line. Sorald clones each
repository and runs Sonar checks on the head of the default branch.

The full list of options is as follows (and can also be found by running `sorald
mine --help`).

```bash
      --git-repos-list=<reposList>
                             The path to the repos list.
  -h, --help                 Show this help message and exit.
      --handled-rules        When this argument is used, Sorald only mines
                               violations of the rules that can be fixed by
                               Sorald.
      --miner-output-file=<minerOutputFile>
                             The path to the output file.
      --resolve-classpath    Resolve the classpath of a project for more
                               accurate scans. Currently only works for Maven
                               projects.
      --rule-types=<ruleTypes>[,<ruleTypes>...]
                             One or more types of rules to check for (use ','
                               to separate multiple types). Choices: BUG,
                               VULNERABILITY, CODE_SMELL, SECURITY_HOTSPOT
      --source=<source>      The path to the file or folder to be analyzed and
                               possibly repaired.
      --stats-on-git-repos   If the stats should be computed on git repos.
      --stats-output-file=<statsOutputFile>
                             Path to a file to store execution statistics in
                               (in JSON format). If left unspecified, Sorald
                               does not gather statistics.
      --target=<target>      The target of this execution (ex. sorald/92d377).
                               This will be included in the json report.
      --temp-dir=<tempDir>   The path to the temp directory.
  -V, --version              Print version information and exit.
```

#### Running Sorald on GitHub projects to propose PRs with fixes

To run Sorald on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sorald is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Academic bibliographic references

"[A template-based approach to automatic program repair of Sonarqube static warnings](http://kth.diva-portal.org/smash/get/diva2:1433710/FULLTEXT01.pdf)", by Haris Adzemovic, Master's thesis, KTH, School of Electrical Engineering and Computer Science (EECS), 2020. [(bibtex)](http://www.diva-portal.org/smash/references?referenceFormat=BIBTEX&pids=[diva2:1433710]&fileName=export.txt)

### Experiments with Sorald
[Sorald-Experiments repository](https://github.com/khaes-kth/Sorald-experiments) includes the data related to our experiments with Sorald that are part of a recently conducted research project.

## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
