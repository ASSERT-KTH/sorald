package sonarquberepair;

import org.junit.Test;
import org.junit.Assert;

import java.io.File;

public class FileOutputStrategyTest {

	@Test
	public void test() throws Exception {
		Main.main(new String[]{
			"--originalFilesPath",Constants.PATH_TO_FILE,
			"--ruleKeys","2111",
			"--workspace","ChangedOnlyFilesOutput",
			"--gitRepoPath","."});

		File file = new File("ChangedOnlyFilesOutput" + File.separator + "spooned");
		Assert.assertEquals(file.list().length,1);

		file = new File("ChangedOnlyFilesOutput" + File.separator + "SonarGitPatches");
		Assert.assertEquals(file.list().length,1);
	}

}
