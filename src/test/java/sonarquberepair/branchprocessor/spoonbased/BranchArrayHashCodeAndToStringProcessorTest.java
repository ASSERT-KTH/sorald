package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class BranchArrayHashCodeAndToStringProcessorTest {

	@Test
	public void arrayToStringProcessorTest() throws Exception {
		String fileName = "ArrayHashCodeAndToString.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String workspace = "sonar-branch-workspace";
		String pathToRepairedFile = workspace + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
			"--versionMode","NEW",
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","2116",
			"--workspace",workspace});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

	@Test
	public void arrayToStringProcessorTest2() throws Exception {
		String fileName = "CodeFactory.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String workspace = "sonar-branch-workspace";
		String pathToRepairedFile = workspace + "/spooned/spoon/reflect/factory/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
			"--versionMode","NEW",
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","2116",
			"--prettyPrintingStrategy","SNIPER",
			"--workspace",workspace});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

}
