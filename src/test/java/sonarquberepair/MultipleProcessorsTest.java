package sonarquberepair;

import org.junit.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class MultipleProcessorsTest {

	@Test
	public void test_threeExistingRules() throws Exception {
		String fileName = "MultipleProcessors.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		Main.main(new String[]{
				"--originalFilesPath",pathToBuggyFile,
				"--ruleKeys","2111,2184,2204",
				"--prettyPrintingStrategy","SNIPER",
				"--workspace",Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new EqualsOnAtomicClassCheck());
	}

}
