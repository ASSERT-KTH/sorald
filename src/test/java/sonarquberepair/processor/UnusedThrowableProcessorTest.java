package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.unused.UnusedThrowableCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class UnusedThrowableProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "UnusedThrowable.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new UnusedThrowableCheck());
		Main.main(new String[]{
				"--originalFilesPath",pathToBuggyFile,
				"--ruleKeys","3984",
				"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnusedThrowableCheck());
	}

}
