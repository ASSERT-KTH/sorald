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
		String pathToBuggyFile = Constants.PATH_TO_BUGGY_FILES + fileName;
		String pathToRepairedFile = Constants.SONAR_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2111,2184,2204",
				Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.SNIPER.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SONAR_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new EqualsOnAtomicClassCheck());
	}

}
