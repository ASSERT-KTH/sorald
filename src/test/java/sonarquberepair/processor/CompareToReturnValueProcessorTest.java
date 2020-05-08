package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.CompareToReturnValueCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class CompareToReturnValueProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "CompareToReturnValue.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		System.out.println(pathToBuggyFile);
		JavaCheckVerifier.verify(pathToBuggyFile, new CompareToReturnValueCheck());
		Main.main(new String[]{
			"--originalFilesPath",pathToBuggyFile,
			"--ruleKeys","2167",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CompareToReturnValueCheck());
	}

}
