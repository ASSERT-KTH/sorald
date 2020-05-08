package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class BigDecimalDoubleConstructorProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "BigDecimalDoubleConstructor.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new BigDecimalDoubleConstructorCheck());
		Main.main(new String[]{
			"--originalFilesPath",pathToBuggyFile,
			"--ruleKeys","2111",
			"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
	}

}
