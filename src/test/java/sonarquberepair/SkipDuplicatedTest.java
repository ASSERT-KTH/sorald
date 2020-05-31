package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.PrettyPrintingStrategy;
import sonarquberepair.TestHelper;

public class SkipDuplicatedTest {
	private static final String WORKSPACE = "duplicated-types-ws";

	@Test
	public void arrayToStringProcessorTestSkipDuplicatedWithSuccess() throws Exception {
		String fileName = "ArrayHashCodeAndToString.java";
		String pathToBuggyFile = Constants.PATH_TO_DUPLICATED_FILES + fileName;
		String pathToRepairedFile = this.WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,Constants.PATH_TO_DUPLICATED_FILES,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2116",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,this.WORKSPACE,
				Constants.ARG_SYMBOL + Constants.ARG_SKIP_DUPLICATED_TYPES});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

	@Test(expected = spoon.compiler.ModelBuildingException.class)
	public void arrayToStringProcessorTestSkipDuplicatedWithFailure() throws Exception {
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,Constants.PATH_TO_DUPLICATED_FILES,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2116",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,this.WORKSPACE });
	}
}
