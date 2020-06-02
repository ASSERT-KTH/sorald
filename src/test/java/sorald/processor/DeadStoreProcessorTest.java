package sorald.processor;

import org.junit.Test;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sorald.Constants;
import sorald.Main;

public class DeadStoreProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "DeadStores.java";
		String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new DeadStoreCheck());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"1854",
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SORALD_WORKSPACE});
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new DeadStoreCheck());
	}

}
