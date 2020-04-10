package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;
import org.junit.Assert;

import java.io.File;

public class OnlyChangedFilesOutputedTest {

	@Test
	public void test() throws Exception {

		Main.main(new String[]{
			"--repairPath",Constants.PATH_TO_FILE,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleNumbers","2111",
			"--workspace","OnlyChangedFilesOutput"});

		File file = new File("OnlyChangedFilesOutput" + File.separator + "spooned");
		Assert.assertEquals(file.list().length,1);
	}

}
