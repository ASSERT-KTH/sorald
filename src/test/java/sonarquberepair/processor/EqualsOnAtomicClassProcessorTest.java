package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class EqualsOnAtomicClassProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "EqualsOnAtomicClass.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new EqualsOnAtomicClassCheck());
		Main.main(new String[]{
				"--originalFilesPath", pathToBuggyFile,
				"--ruleKeys", "2204",
				"--prettyPrintingStrategy", "SNIPER",
				"--workspace", Constants.WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new EqualsOnAtomicClassCheck());
	}

}
