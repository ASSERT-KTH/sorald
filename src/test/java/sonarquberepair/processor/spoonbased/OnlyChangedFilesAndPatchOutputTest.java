package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import org.junit.Assert;

import java.io.File;

public class OnlyChangedFilesAndPatchOutputTest {

	@Test
	public void test() throws Exception {
		Main.main(new String[]{
			"--originalFilesPath",Constants.PATH_TO_FILE,
			"--projectKey",Constants.PROJECT_KEY,
			"--ruleKeys","2111",
			"--workspace","ChangedOnlyFilesOutput",
			"--gitRepoPath","."});

		File file = new File("ChangedOnlyFilesOutput" + File.separator + "spooned");
		Assert.assertEquals(file.list().length,1);

		file = new File("ChangedOnlyFilesOutput" + File.separator + "SonarGitPatches");
		Assert.assertEquals(file.list().length,1);
	}

}
