# Sonarqube-repair

Sonarqube-repair is a collection of java code transformations made with the [Spoon](https://github.com/INRIA/spoon) library to repair violations of static rules defined by [Sonarqube](https://rules.sonarsource.com).

## Handled Sonarqube rules

Sonarqube-repair can currently repair violations of 7 Sonarqube rules of which 5 are labeled as `BUG` and 2 as `Code Smell`. [Checkout out the handled rules](HANDLED_SONARQUBE_RULES.md).

## Getting Started
### Prerequisites 
A JDK (java 1.8 or above) 

### Usage
If you wish to run Sonarqube-repair on your own project, look at [this Git repository](https://github.com/HarisAdzemovic/SQ-Repair-CI-Integration) for en example. In it, Sonarqube-repair is ran on the three Apache projects defined in the *projects_for_model_1.txt* file.

##### If you want to run it from your IDE

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

Import it as maven project in your favorite Java IDE.
Create the folder `sonarqube-repair/source/act/`
Put files to be repaired in the new folder.
Run the main function. 

You can either give command line arguments or set the rule number and project-key manually in the main function in `src/main/java/Main.java`. The repaired files will appear in `sonarqube-repair/spooned/` . Also you need to set the url of the sonar analysis of your project in `src/main/java/ParseAPI.java` if it is different from [sonarcloud.io](https://sonarcloud.io/about)
 
## Running the tests

run `mvn test` to run the tests for all repairs.

## Contributing

### Issues 
Feel free to open issues on this github repository.

### Pull requests
Pull requests for improvements or new features are welcome by all contributers.

## Authors
* Ashutosh Kumar Verma [ashutosh1598](https://github.com/ashutosh1598)
* Martin Monperrus [monperrus](https://github.com/monperrus)
* Pavel Pvojtechovsky  [pvojtechovsky](https://github.com/pvojtechovsky)
* Haris Adzemovic [HarisAdzemovic](https://github.com/HarisAdzemovic)

## Implementation notes
#### This only applies to the 2 Code Smells and Bug S2095
Sonarjava had to be changed in order to return the appropriate issue information for offline-repair. The changes are at https://github.com/kth-tcs/sonar-java/pull/1 . Now I(Ashutosh) don't think that doing offline-repair is a good idea and we should stick to using the web api. I don't know of a way to offline detect issues which have their components in more than one file.

## Case studies

Spoon SonarQube: <https://sonarqube.ow2.org/project/issues?id=fr.inria.gforge.spoon%3Aspoon-core&resolved=false&types=BUG>
