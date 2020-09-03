package sorald.processor;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;

public class NoSonarTest {
	@Test
	public void noSonarTesting() throws Exception {
		String fileName = "NOSONARCommentTest.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2116",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SORALD_WORKSPACE,
				Constants.ARG_SYMBOL + Constants.ARG_MAX_FIXES_PER_RULE,"3"});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck()); // one bug left
	}
}
