package sorald;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileOutputStrategyTest {

    @Test
    public void test_onlyChangedFilesAndPatchOutput() throws Exception {
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "4973",
                    Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.CHANGED_ONLY.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE,
                    Constants.ARG_SYMBOL + Constants.ARG_GIT_REPO_PATH,
                    "."
                });

        File spooned = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.SPOONED);
        Assertions.assertEquals(spooned.list().length, 1);

        File patches = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.PATCHES);
        Assertions.assertEquals(patches.list().length, 1);
    }

    @Test
    public void test_onlyChangedFilesAndNoPatchOutput() throws Exception {
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "4973",
                    Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.CHANGED_ONLY.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE
                });

        File spooned = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.SPOONED);
        Assertions.assertEquals(spooned.list().length, 1);

        File patches = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.PATCHES);
        Assertions.assertNull(patches.list());
    }

    @Test
    public void test_allFilesAndNoPatchOutput() throws Exception {
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "4973",
                    Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.ALL.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    PrettyPrintingStrategy.NORMAL.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE
                });

        File spooned = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.SPOONED);
        Assertions.assertTrue(spooned.list().length > 1);

        File patches = new File(Constants.SORALD_WORKSPACE + File.separator + Constants.PATCHES);
        Assertions.assertNull(patches.list());
    }
}
