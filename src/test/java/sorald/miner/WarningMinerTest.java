package sorald.miner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sorald.Constants;
import sorald.FileUtils;
import sorald.Main;
import sorald.cli.SoraldVersionProvider;
import sorald.event.StatsMetadataKeys;
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

    @Test
    public void test_onlyMineRepairableViolations() throws Exception {
        File outputFile = File.createTempFile("warnings", null),
                temp = Files.createTempDirectory("tempDir").toFile();

        String fileName = "warning_miner/test_repos.txt";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        fileName = "warning_miner/test_results.txt";
        File correctResults = new File(Constants.PATH_TO_RESOURCES_FOLDER + fileName);

        runMiner(pathToRepos, outputFile.getPath(), temp.getPath(), Constants.ARG_HANDLED_RULES);

        List<String> actualLines = extractSortedNonZeroChecks(outputFile.toPath());
        List<String> expectedLines = extractSortedNonZeroChecks(correctResults.toPath());

        expectedLines =
                expectedLines.stream()
                        .filter(
                                line -> {
                                    try {
                                        Class.forName(
                                                "sorald.processor."
                                                        + line.split("=")[0].replace(
                                                                "Check", "Processor"));
                                        return true;
                                    } catch (ClassNotFoundException e) {
                                        return false;
                                    }
                                })
                        .collect(Collectors.toList());

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
                Constants.ARG_RULE_TYPES,
                checkTypes.stream().map(Checks.CheckType::name).collect(Collectors.joining(",")));

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

    /**
     * Tests that the warnings miner can handle searching a project in which a directory name ends
     * with ".java". See https://github.com/SpoonLabs/sorald/issues/207 for context.
     */
    @Test
    public void warningsMiner_canAnalyzeFile_inDirectoryWithJavaExtension(@TempDir File workdir)
            throws Exception {
        File dirWithJavaExtension =
                workdir.toPath().resolve("project" + Constants.JAVA_EXT).toFile();
        assertTrue(dirWithJavaExtension.mkdir(), "failed to create test directory");
        Files.writeString(
                dirWithJavaExtension.toPath().resolve("Main.java"),
                "public class Main { double a = 1f / 2f; }");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Main.main(
                new String[] {
                    Constants.MINE_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.toString()
                });

        assertThat(out.toString(), containsString("MathOnFloatCheck=1"));
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

    /** Test that extracting warnings gives results even for rules that are not violated. */
    @Test
    public void extractWarnings_statsOutput_containsExpectedAttributes() throws Exception {
        File outputFile = File.createTempFile("warnings", null),
                temp = Files.createTempDirectory("tempDir").toFile(),
                statsFile = File.createTempFile("stats", null);
        String fileName = "warning_miner/test_repos.txt";
        String pathToRepos = Constants.PATH_TO_RESOURCES_FOLDER + fileName;

        runMiner(
                pathToRepos,
                outputFile.getPath(),
                temp.getPath(),
                Constants.ARG_STATS_OUTPUT_FILE,
                statsFile.getPath());

        JSONObject jo = FileUtils.readJSON(statsFile.toPath());
        JSONObject executionInfo = jo.getJSONObject(StatsMetadataKeys.EXECUTION_INFO);

        assertThat(
                executionInfo.get(StatsMetadataKeys.SORALD_VERSION),
                equalTo(
                        SoraldVersionProvider.getVersionFromPropertiesResource(
                                SoraldVersionProvider.DEFAULT_RESOURCE_NAME)));
        assertThat(
                executionInfo.get(StatsMetadataKeys.JAVA_VERSION),
                equalTo(System.getProperty(Constants.JAVA_VERSION_SYSTEM_PROPERTY)));
        assertThat(
                executionInfo.getJSONArray(StatsMetadataKeys.ORIGINAL_ARGS).toList().size(),
                greaterThan(0));
        assertThat(jo.getLong(StatsMetadataKeys.TOTAL_MINING_TIME), greaterThan(0L));
        assertThat(jo.getJSONArray(StatsMetadataKeys.MINED_RULES).toList().size(), greaterThan(0));
        assertTrue(jo.has(StatsMetadataKeys.MINING_START_TIME));
        assertTrue(jo.has(StatsMetadataKeys.MINING_END_TIME));
    }

    private static void runMiner(
            String pathToRepos, String pathToOutput, String pathToTempDir, String... extraArgs)
            throws Exception {
        String[] baseArgs =
                new String[] {
                    Constants.MINE_COMMAND_NAME,
                    Constants.ARG_STATS_ON_GIT_REPOS,
                    Constants.ARG_GIT_REPOS_LIST,
                    pathToRepos,
                    Constants.ARG_MINER_OUTPUT_FILE,
                    pathToOutput,
                    Constants.ARG_TEMP_DIR,
                    pathToTempDir
                };
        String[] fullArgs =
                Stream.of(baseArgs, extraArgs).flatMap(Arrays::stream).toArray(String[]::new);
        Main.main(fullArgs);
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
