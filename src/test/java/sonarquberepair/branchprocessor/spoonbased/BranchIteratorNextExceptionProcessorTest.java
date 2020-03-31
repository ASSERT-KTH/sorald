package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.IteratorNextExceptionCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class BranchIteratorNextExceptionProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "IteratorNextException.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String workspace = "sonar-branch-workspace";
		String pathToRepairedFile = workspace + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new IteratorNextExceptionCheck());
		Main.main(new String[]{
			"--versionMode","NEW",
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","2272",
			"--workspace",workspace});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new IteratorNextExceptionCheck());
	}

}
