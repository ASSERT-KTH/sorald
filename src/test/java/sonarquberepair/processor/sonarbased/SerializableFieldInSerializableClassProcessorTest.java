package sonarquberepair.processor.sonarbased;

import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class SerializableFieldInSerializableClassProcessorTest {

	@Test
	public void test() throws Exception {

		String fileName = "SerializableFieldProcessorTest.java";
		String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
		String workspace = "sonar-branch-workspace";
		String pathToRepairedFile = workspace + "/spooned/" + fileName;

		JavaCheckVerifier.verify(pathToBuggyFile, new SerializableFieldInSerializableClassCheck());
		Main.main(new String[]{
			"--repairPath",pathToBuggyFile,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","1948",
			"--workspace",workspace});
		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SerializableFieldInSerializableClassCheck());
	}

}
