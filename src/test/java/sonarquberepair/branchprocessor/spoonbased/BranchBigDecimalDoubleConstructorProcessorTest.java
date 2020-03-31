package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class BranchBigDecimalDoubleConstructorProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "BigDecimalDoubleConstructor.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = "./spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new BigDecimalDoubleConstructorCheck());
		Main.main(new String[]{
			"--versionMode","NEW",
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","2111",
			"--repairMode","SNIPER"});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
	}

}
