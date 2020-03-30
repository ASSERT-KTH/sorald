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
		String pathToRepairedFile = "./spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new CompareStringsBoxedTypesWithEqualsCheck());
		Main.repair(pathToBuggyFile, Constants.PROJECT_KEY, 4973, false);
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CompareStringsBoxedTypesWithEqualsCheck());
	}

}
