package sorald.miner;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import sorald.Constants;
import sorald.miner.MineSonarWarnings;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

public class WarningMinerTest {

    @Test
    public void test_warningMiner() throws Exception {
        File outputFile = File.createTempFile("warnings", null),
                temp = Files.createTempDirectory("tempDir").toFile();

        String fileName = "warning_miner/test_repos";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        fileName = "warning_miner/test_results";
        File correctResults = new File(Constants.PATH_TO_RESOURCES_FOLDER + fileName);

        MineSonarWarnings.main(new String[] {
                Constants.ARG_SYMBOL + Constants.ARG_STATS_ON_GIT_REPOS, "true",
                Constants.ARG_SYMBOL + Constants.ARG_GIT_REPOS_LIST, pathToRepos,
                Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE, outputFile.getPath(),
                Constants.ARG_SYMBOL + Constants.ARG_TEMP_DIR, temp.getPath()
        });

        assertTrue(FileUtils.contentEquals(correctResults, outputFile));
    }
}
