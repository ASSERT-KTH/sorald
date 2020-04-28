package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.checks.SelfAssignementCheck;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class SelfAssignementProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "SelfAssignement.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new SelfAssignementCheck());
		Main.main(new String[]{
			"--originalFilesPath",pathToBuggyFile,
			"--ruleKeys","1656",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SelfAssignementCheck());
	}

}
