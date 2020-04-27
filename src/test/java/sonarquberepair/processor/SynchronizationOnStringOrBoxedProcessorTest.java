package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.SynchronizationOnStringOrBoxedCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class SynchronizationOnStringOrBoxedProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "SynchronizationOnStringOrBoxed.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new SynchronizationOnStringOrBoxedCheck());
		Main.main(new String[]{
				"--originalFilesPath", pathToBuggyFile,
				"--ruleKeys", "1860",
				"--workspace", Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SynchronizationOnStringOrBoxedCheck());
	}

}
