package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.GetClassLoaderCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class GetClassLoaderProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "GetClassLoader.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new GetClassLoaderCheck());
		Main.main(new String[]{
			"--originalFilesPath",pathToBuggyFile,
			"--ruleKeys","3032",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new GetClassLoaderCheck());
	}

}
