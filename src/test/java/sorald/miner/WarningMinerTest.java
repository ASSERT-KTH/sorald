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
import java.util.stream.Stream;
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

    /**
     * Test that the warnings miner respects the --ruleTypes option, and only uses checks of that
     * type when given.
     */
    @Test
    public void warningsMiner_onlyScansForGivenTypes_whenRuleTypesGiven() throws Exception {
        List<Checks.CheckType> checkTypes =
                Arrays.asList(Checks.CheckType.VULNERABILITY, Checks.CheckType.CODE_SMELL);

        String fileName = "warning_miner/test_repos.txt";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        File outputFile = File.createTempFile("warnings", null);
        File temp = Files.createTempDirectory("tempDir").toFile();

        runMiner(
                pathToRepos,
                outputFile.getPath(),
                temp.getPath(),
                Constants.ARG_SYMBOL + Constants.ARG_RULE_TYPES,
                checkTypes.stream()
                        .map(Checks.CheckType::getLabel)
                        .collect(Collectors.joining(",")));

        List<String> expectedChecks =
                checkTypes.stream()
                        .map(Checks::getChecksByType)
                        .flatMap(List::stream)
                        .map(Class::getSimpleName)
                        .sorted()
                        .collect(Collectors.toList());
        List<String> actualChecks = extractSortedCheckNames(outputFile.toPath());

        assertThat(actualChecks, equalTo(expectedChecks));
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
        List<String> actualChecks = extractSortedCheckNames(outputFile.toPath());

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

    private static void runMiner(
            String pathToRepos, String pathToOutput, String pathToTempDir, String... extraArgs)
            throws Exception {
        String[] baseArgs =
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_STATS_ON_GIT_REPOS,
                    "true",
                    Constants.ARG_SYMBOL + Constants.ARG_GIT_REPOS_LIST,
                    pathToRepos,
                    Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
                    pathToOutput,
                    Constants.ARG_SYMBOL + Constants.ARG_TEMP_DIR,
                    pathToTempDir
                };
        String[] fullArgs =
                Stream.of(baseArgs, extraArgs).flatMap(Arrays::stream).toArray(String[]::new);
        MineSonarWarnings.main(fullArgs);
    }

    /** Extract check names from the warnings miner output file, sorted lexicographically. */
    private static List<String> extractSortedCheckNames(Path outputFile) throws IOException {
        Pattern checkNamePattern = Pattern.compile("^(.*)=\\d+$");
        return Files.readAllLines(outputFile).stream()
                .map(checkNamePattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .sorted()
                .collect(Collectors.toList());
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
