package sorald.miner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.java.AnalysisException;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.sonar.Checks;

public class WarningMinerTest {

    @Test
    public void test_warningMiner() throws Exception {
        File outputFile = File.createTempFile("warnings", null),
                temp = Files.createTempDirectory("tempDir").toFile();

        String fileName = "warning_miner/test_repos.txt";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        fileName = "warning_miner/test_results.txt";
        File correctResults = new File(Constants.PATH_TO_RESOURCES_FOLDER + fileName);

        runMiner(pathToRepos, outputFile.getPath(), temp.getPath());

        List<String> expectedLines = extractSortedNonZeroChecks(correctResults.toPath());
        List<String> actualLines = extractSortedNonZeroChecks(outputFile.toPath());

        assertFalse(expectedLines.isEmpty(), "sanity check failure, expected output is empty");
        assertThat(actualLines, equalTo(expectedLines));
    }

    /** Test that extracting warnings gives results even for rules that are not violated. */
    @Test
    public void extractWarnings_accountsForAllRules_whenManyAreNotViolated() throws Exception {
        File outputFile = File.createTempFile("warnings", null),
                temp = Files.createTempDirectory("tempDir").toFile();
        String fileName = "warning_miner/test_repos.txt";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;

        runMiner(pathToRepos, outputFile.getPath(), temp.getPath());

        List<String> expectedChecks =
                Checks.getAllChecks().stream()
                        .map(Class::getSimpleName)
                        .sorted()
                        .collect(Collectors.toList());
        Pattern checkNamePattern = Pattern.compile("^(.*)=\\d+$");
        List<String> actualChecks =
                Files.readAllLines(outputFile.toPath()).stream()
                        .map(checkNamePattern::matcher)
                        .filter(Matcher::matches)
                        .map(m -> m.group(1))
                        .sorted()
                        .collect(Collectors.toList());

        assertThat(actualChecks, equalTo(expectedChecks));
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    public void extractWarnings_throwsException_whenCheckCrashes() {
        JavaFileScanner crashyCheck =
                context -> {
                    throw new IllegalStateException();
                };

        assertThrows(
                AnalysisException.class,
                () ->
                        MineSonarWarnings.extractWarnings(
                                Constants.PATH_TO_RESOURCES_FOLDER, Arrays.asList(crashyCheck)));
    }

    private static void runMiner(String pathToRepos, String pathToOutput, String pathToTempDir)
            throws Exception {
        MineSonarWarnings.main(
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_STATS_ON_GIT_REPOS,
                    "true",
                    Constants.ARG_SYMBOL + Constants.ARG_GIT_REPOS_LIST,
                    pathToRepos,
                    Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
                    pathToOutput,
                    Constants.ARG_SYMBOL + Constants.ARG_TEMP_DIR,
                    pathToTempDir
                });
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
