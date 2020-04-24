package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.UnclosedResourcesCheck;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class UnclosedResourcesProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "ZipFolder.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new UnclosedResourcesCheck());
		Main.main(new String[]{
			"--originalFilesPath",pathToBuggyFile,
			"--ruleKeys","2095",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnclosedResourcesCheck());
	}

}
