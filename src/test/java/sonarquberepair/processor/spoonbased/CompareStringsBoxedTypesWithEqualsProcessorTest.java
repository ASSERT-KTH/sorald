package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class CompareStringsBoxedTypesWithEqualsProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "CompareStringsBoxedTypesWithEquals.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String workspace = "sonar-branch-workspace";
		String pathToRepairedFile = workspace + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new CompareStringsBoxedTypesWithEqualsCheck());
		Main.main(new String[]{
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","4973",
			"--workspace",workspace});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CompareStringsBoxedTypesWithEqualsCheck());
	}

}
