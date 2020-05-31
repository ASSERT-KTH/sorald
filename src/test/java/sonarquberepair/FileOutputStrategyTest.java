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
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,Constants.PATH_TO_BUGGY_FILES,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"4973",
				Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY, FileOutputStrategy.CHANGED_ONLY.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, fileOutputStrategyTestWorkspace,
				Constants.ARG_SYMBOL + Constants.ARG_GIT_REPO_PATH,"."});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.SPOONED);
		Assert.assertEquals(spooned.list().length,1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.PATCHES);
		Assert.assertEquals(patches.list().length,1);
	}

	@Test
	public void test_onlyChangedFilesAndNoPatchOutput() throws Exception {
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,Constants.PATH_TO_BUGGY_FILES,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"4973",
				Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY, FileOutputStrategy.CHANGED_ONLY.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, fileOutputStrategyTestWorkspace});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.SPOONED);
		Assert.assertEquals(spooned.list().length,1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.PATCHES);
		Assert.assertNull(patches.list());
	}

	@Test
	public void test_allFilesAndNoPatchOutput() throws Exception {
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,Constants.PATH_TO_BUGGY_FILES,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"4973",
				Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY, FileOutputStrategy.ALL.name(),
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, fileOutputStrategyTestWorkspace});

		File spooned = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.SPOONED);
		Assert.assertTrue(spooned.list().length > 1);

		File patches = new File(fileOutputStrategyTestWorkspace + File.separator + Constants.PATCHES);
		Assert.assertNull(patches.list());
	}

}
