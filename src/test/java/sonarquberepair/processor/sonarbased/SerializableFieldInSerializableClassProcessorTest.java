package sonarquberepair.processor.sonarbased;

import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.PrettyPrintingStrategy;
import sonarquberepair.TestHelper;

public class SerializableFieldInSerializableClassProcessorTest {

	@Test
	public void test() throws Exception {
		String fileName = "SerializableFieldProcessorTest.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String pathToRepairedFile = "./spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new SerializableFieldInSerializableClassCheck());
		Main.repair(pathToBuggyFile, Constants.PROJECT_KEY, 1948, PrettyPrintingStrategy.NORMAL);
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SerializableFieldInSerializableClassCheck());
	}

}
