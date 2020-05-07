package sonarquberepair;

import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;

public class FileOutputStrategyTest {

	private String fileOutputStrategyTestWorkspace = "FileOutputStrategyTest";

	@After
	public void tearDown() {
		TestHelper.deleteDirectory(new File(fileOutputStrategyTestWorkspace));
	}

	@Test
	public void test_onlyChangedFilesAndPatchOutput() throws Exception {
		Main.main(new String[]{
			"--originalFilesPath",Constants.PATH_TO_FILE,
			"--ruleKeys","4973",
			"--fileOutputStrategy", FileOutputStrategy.CHANGED_ONLY.name(),
			"--workspace", fileOutputStrategyTestWorkspace,
			"--gitRepoPath","."});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + "spooned");
		Assert.assertEquals(spooned.list().length,1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + "SonarGitPatches");
		Assert.assertEquals(patches.list().length,1);
	}

	@Test
	public void test_onlyChangedFilesAndNoPatchOutput() throws Exception {
		Main.main(new String[]{
				"--originalFilesPath",Constants.PATH_TO_FILE,
				"--ruleKeys","4973",
				"--fileOutputStrategy", FileOutputStrategy.CHANGED_ONLY.name(),
				"--workspace", fileOutputStrategyTestWorkspace});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + "spooned");
		Assert.assertEquals(spooned.list().length,1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + "SonarGitPatches");
		Assert.assertNull(patches.list());
	}

	@Test
	public void test_allFilesAndNoPatchOutput() throws Exception {
		Main.main(new String[]{
				"--originalFilesPath",Constants.PATH_TO_FILE,
				"--ruleKeys","4973",
				"--fileOutputStrategy", FileOutputStrategy.ALL.name(),
				"--workspace", fileOutputStrategyTestWorkspace});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + "spooned");
		Assert.assertTrue(spooned.list().length > 1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + "SonarGitPatches");
		Assert.assertNull(patches.list());
	}

}
