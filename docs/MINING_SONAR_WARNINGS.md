## Mining Sonar Warnings

To mine Sonar warnings in a given project:

1) Prepare a folder with the project source code files; if the project is on GitHub, clone it;

2) Run the mining feature of Sorald:

```bash
$ cd sorald
$ mvn package -DskipTests
$ java -cp target/sorald-1.1-SNAPSHOT-jar-with-dependencies.jar sorald.miner.MineSonarWarnings --originalFilesPath <path to the source code folder of the project>
```

The output will be printed in the screen in the form of a list of Sonar checks/rules in reverse order by the number of warnings found.
