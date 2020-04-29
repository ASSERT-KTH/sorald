# Sonarqube-repair [![Travis Build Status](https://travis-ci.com/SpoonLabs/sonarqube-repair.svg?branch=master)](https://travis-ci.com/SpoonLabs/sonarqube-repair)

Sonarqube-repair is a collection of java code analyses and transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of rules contained in [SonarQube](https://rules.sonarsource.com).

## Handled rules
Sonarqube-repair can currently repair violations of 16 rules of which 14 are labeled as `BUG` and 2 as `Code Smell`. [Check out the handled rules](/docs/HANDLED_RULES.md).

## Getting started

### Prerequisites 

A JDK (java 1.8)

### Usage

##### If you want to run it from your IDE

1) Clone this repository: `git clone https://github.com/SpoonLabs/sonarqube-repair.git`

2) Run the script `./init.sh`:

```bash
$ chmod +x ./init.sh
$ ./init.sh
```

3) Import it as Maven project in your Java IDE

4) Create the folder `sonarqube-repair/source/act/`

5) Add files to be repaired in the new folder

6) In the terminal, run the main Sonarqube-repair function:

 ```bash
$ cd sonarqube-repair
$ mvn package -DskipTests
$ ls target/*jar
target/sonarqube-repair-1.1-SNAPSHOT.jar
target/sonarqube-repair-1.1-SNAPSHOT-jar-with-dependencies.jar
target/sonarqube-repair-1.1-SNAPSHOT-javadoc.jar
$ java -jar target/sonarqube-repair-1.1-SNAPSHOT-jar-with-dependencies.jar <arguments>
 ```

For the arguments, provide the Sonar rule key (see the supported rules [here](/docs/HANDLED_RULES.md)).
The repaired files will appear in `sonarqube-repair/spooned/`.
 
##### If you want to run it on GitHub projects to propose PRs with fixes

To run Sonarqube-repair on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer (see instructions [here](/docs/CONTRIBUTING.md)).
