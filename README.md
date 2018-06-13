# sonarqube-repair

This is the initial draft of the system to repair sonarqube bugs.

To use it, run:

`git clone https://github.com/kth-tcs/sonarqube-repair.git`

`cd sonarqube-repair`

Then open the src/main/java/Main.java file. Change the directory in the statement `launcher.addInputResource("/home/ashutosh/eclipse-workspace/spoon1/source/act/");`
to the source files that you want to repair. Also change the rule in `launcher.addProcessor()` to the rule you want to repair.

Then do :

`mvn -U clean package`

`cd target`

`java -jar spoon1-1.0-SNAPSHOT-jar-with-dependencies.jar`

The repaired code will appear in sonarqube-repair/spooned/

Feel free to open issues.
