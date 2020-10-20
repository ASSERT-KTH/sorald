package sorald.miner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import sorald.Constants;

public class WarningMinerTest {

  @Test
  public void test_warningMiner() throws Exception {
    File outputFile = File.createTempFile("warnings", null),
        temp = Files.createTempDirectory("tempDir").toFile();

    String fileName = "warning_miner/test_repos.txt";
    String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
    fileName = "warning_miner/test_results.txt";
    File correctResults = new File(Constants.PATH_TO_RESOURCES_FOLDER + fileName);

    MineSonarWarnings.main(
        new String[] {
          Constants.ARG_SYMBOL + Constants.ARG_STATS_ON_GIT_REPOS,
          "true",
          Constants.ARG_SYMBOL + Constants.ARG_GIT_REPOS_LIST,
          pathToRepos,
          Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
          outputFile.getPath(),
          Constants.ARG_SYMBOL + Constants.ARG_TEMP_DIR,
          temp.getPath()
        });

    List<String> expectedLines = extractSortedNonZeroChecks(correctResults.toPath());
    List<String> actualLines = extractSortedNonZeroChecks(outputFile.toPath());

    assertFalse("sanity check failure, expected output is empty", expectedLines.isEmpty());
    assertThat(actualLines, equalTo(expectedLines));
  }

  /**
   * Extract all lines from a warning miner results file for which the check has >0 detections,
   * sorted lexicographically. Expect each relevant line to be on the form "SomeSonarCheck=%d",
   * where %d is any non-negative integer.
   */
  private static List<String> extractSortedNonZeroChecks(Path minerResults) throws IOException {
    return Files.readAllLines(minerResults).stream()
        .filter(s -> s.matches("^.*=\\d+$"))
        .filter(s -> !s.matches("^.*=0\\s*$"))
        .sorted()
        .collect(Collectors.toList());
  }
}
