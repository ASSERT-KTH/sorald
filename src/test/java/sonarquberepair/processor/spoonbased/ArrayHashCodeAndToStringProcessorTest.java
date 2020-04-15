package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class ArrayHashCodeAndToStringProcessorTest {

	@Test
	public void arrayToStringProcessorTest() throws Exception {
		String fileName = "ArrayHashCodeAndToString.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleKeys","2116",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

	@Test
	public void arrayToStringProcessorTest2() throws Exception {
		String fileName = "CodeFactory.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/spoon/reflect/factory/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleKeys","2116",
			"--prettyPrintingStrategy","SNIPER",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

}
