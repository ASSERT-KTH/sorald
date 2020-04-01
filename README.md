# Sonarqube-repair

Sonarqube-repair is a collection of java code transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of static rules defined by [SonarQube](https://rules.sonarsource.com).

## Handled SonarQube rules

Sonarqube-repair can currently repair violations of 7 SonarQube rules of which 5 are labeled as `BUG` and 2 as `Code Smell`. [Checkout out the handled rules](/docs/HANDLED_SONARQUBE_RULES.md).

## Getting Started
### Prerequisites 
A JDK (java 1.8 or above) 

### Usage

##### If you want to run it from your IDE

1) Clone this repository: `git clone https://github.com/kth-tcs/sonarqube-repair.git`

2) Import it as Maven project in your Java IDE

3) Create the folder `sonarqube-repair/source/act/`

4) Add files to be repaired in the new folder

5) In the terminal, run the main Sonarqube-repair function:

 ```bash
$ cd sonarqube-repair
$ mvn package -DskipTests
$ ls target/*jar
target/sonarqube-repair-0.1-SNAPSHOT.jar  target/sonarqube-repair-0.1-SNAPSHOT-jar-with-dependencies.jar  target/sonarqube-repair-0.1-SNAPSHOT-javadoc.jar
$ java -jar target/sonarqube-repair-0.1-SNAPSHOT-jar-with-dependencies.jar <arguments>
 ```

For the arguments, provide the Sonar rule key (see the supported rules [here](/docs/HANDLED_SONARQUBE_RULES.md)).
For the rules 2095, 1854, and 1948, also provide the project key for the Sonar analysis (the url of the Sonar analysis of your project should be set in `src/main/java/sonarquberepair/processor/sonarbased/SonarWebAPIBasedProcessor.java` if it is different from [sonarcloud.io](https://sonarcloud.io/about)).
 
Finally, the repaired files will appear in `sonarqube-repair/spooned/`.
 
##### If you want to run it on GitHub projects to propose PRs with fixes

To run Sonarqube-repair on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer.

## Authors
* Ashutosh Kumar Verma ([@ashutosh1598](https://github.com/ashutosh1598))
* Martin Monperrus ([@monperrus](https://github.com/monperrus))
* Pavel Pvojtechovsky ([@pvojtechovsky](https://github.com/pvojtechovsky))
* Haris Adzemovic ([@HarisAdzemovic](https://github.com/HarisAdzemovic))

## Implementation notes

Implementation notes can be found [here](/docs/IMPLEMENTATION_NOTES.md).
