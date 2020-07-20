package sorald.processor;

import org.junit.Test;
import org.sonar.java.checks.InterruptedExceptionCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sorald.Constants;
import sorald.FileOutputStrategy;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;

public class InterruptedExceptionProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "InterruptedExceptionForTesting.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new InterruptedExceptionCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2142",
				Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.NORMAL.name(),
				Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY, FileOutputStrategy.ALL.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SORALD_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new InterruptedExceptionCheck());
	}

}
