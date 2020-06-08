package sorald.processor;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.checks.SelfAssignementCheck;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;

public class SelfAssignementProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "SelfAssignement.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new SelfAssignementCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"1656",
				Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.NORMAL.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SORALD_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SelfAssignementCheck());
	}

}
