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

5) Run the main Sonarqube-repair function. 

You can either give command line arguments or set the rule number and project-key manually in the main function in `src/main/java/sonarquberepair/Main.java`. The repaired files will appear in `sonarqube-repair/spooned/` . Also you need to set the url of the sonar analysis of your project in `src/main/java/ParseAPI.java` if it is different from [sonarcloud.io](https://sonarcloud.io/about)
 
##### If you want to run it on GitHub projects to propose PRs with fixes

If you wish to run Sonarqube-repair on projects towards proposing fixes in the form of PRs, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for an example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.
 
## Contributing

Contributions are welcome! Feel free to open issues on this GitHub repository, and also to open pull requests for making this project nicer.

## Authors
* Ashutosh Kumar Verma ([@ashutosh1598](https://github.com/ashutosh1598))
* Martin Monperrus ([@monperrus](https://github.com/monperrus))
* Pavel Pvojtechovsky ([@pvojtechovsky](https://github.com/pvojtechovsky))
* Haris Adzemovic ([@HarisAdzemovic](https://github.com/HarisAdzemovic))

## Implementation notes

Implementation notes can be found [here](/docs/IMPLEMENTATION_NOTES.md).
