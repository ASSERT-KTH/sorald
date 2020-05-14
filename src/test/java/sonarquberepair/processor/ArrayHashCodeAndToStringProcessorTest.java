package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.PrettyPrintingStrategy;
import sonarquberepair.TestHelper;

public class ArrayHashCodeAndToStringProcessorTest {

	@Test
	public void arrayToStringProcessorTest() throws Exception {
		String fileName = "ArrayHashCodeAndToString.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SONAR_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2116",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SONAR_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

	@Test
	public void arrayToStringProcessorTest2() throws Exception {
		String fileName = "CodeFactory.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SONAR_WORKSPACE + "/" + Constants.SPOONED +"/spoon/reflect/factory/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2116",
				Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.SNIPER.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SONAR_WORKSPACE});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
	}

}
