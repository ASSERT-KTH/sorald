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
		String pathToBuggyFile = Constants.PATH_TO_BUGGY_FILES + fileName;
		String pathToRepairedFile = Constants.SONAR_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new UnusedThrowableCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"3984",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SONAR_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnusedThrowableCheck());
	}

}
