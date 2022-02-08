# Sorald [![GHA tests Workflow Status](https://github.com/SpoonLabs/sorald/actions/workflows/tests.yml/badge.svg)](https://github.com/SpoonLabs/sorald/actions/workflows/tests.yml) [![Code Coverage](https://codecov.io/gh/SpoonLabs/sorald/branch/master/graph/badge.svg)](https://codecov.io/gh/SpoonLabs/sorald) ![Supported Platforms](https://img.shields.io/badge/platforms-Linux%2C%20macOS%2C%20Windows-blue.svg)
Sorald is a tool to automatically repair violations of static analysis rules checked with [SonarQube](https://rules.sonarsource.com).
It can currently repair violations of [25+ rules](/docs/HANDLED_RULES.md) based on the design described [Sorald: Automatic Patch Suggestions for SonarQube Static Analysis Violations](http://arxiv.org/pdf/2103.12033).

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

Sorald supports macOS, Linux and, Windows.

For running Sorald, all you need is a Java 11+ runtime.

For building Sorald from source, you need a Java 11+ JDK, Maven and Git.

### Getting a Sorald JAR

To run Sorald, you need to first get your hands on the program. You can do this
either by [building from source](#build), or going to the [latest
release](https://github.com/spoonlabs/sorald/releases/latest) and
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
for any occurrence of `sorald` in these instructions.

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

The full list of options is documented [here](/docs/usage/repair.adoc)
(and can also be found by running `sorald repair --help`):

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

The full list of options documented [here](/docs/usage/mine.adoc)
(and can also be found by running `sorald mine --help`).

#### Running Sorald on GitHub projects to propose PRs with fixes

To run Sorald on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sorald is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Academic bibliographic references

"[Sorald: Automatic Patch Suggestions for SonarQube Static Analysis Violations](http://arxiv.org/pdf/2103.12033)" (Khashayar Etemadi, Nicolas Harrand, Simon Larsen, Haris Adzemovic, Henry Luong Phu, Ashutosh Verma, Fernanda Madeiral, Douglas Wikstrom and Martin Monperrus), Technical report, arXiv 2103.12033, 2021. 

"[A template-based approach to automatic program repair of Sonarqube static warnings](http://kth.diva-portal.org/smash/get/diva2:1433710/FULLTEXT01.pdf)", by Haris Adzemovic, Master's thesis, KTH, School of Electrical Engineering and Computer Science (EECS), 2020. [(bibtex)](http://www.diva-portal.org/smash/references?referenceFormat=BIBTEX&pids=[diva2:1433710]&fileName=export.txt)

### Experiments with Sorald
[Sorald-Experiments repository](https://github.com/khaes-kth/Sorald-experiments) includes the data related to our experiments with Sorald that are part of a recently conducted research project.

## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
