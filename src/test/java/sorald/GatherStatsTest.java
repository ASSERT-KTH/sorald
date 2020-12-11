package sorald;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.event.StatsMetadataKeys;
import sorald.processor.ProcessorTestHelper;
import sorald.processor.SoraldAbstractProcessor;
import sorald.processor.XxeProcessingProcessor;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;

public class GatherStatsTest {
    @Test
    public void statisticsFile_containsExpectedStats(@TempDir File tempDir) throws Exception {
        ProcessorTestHelper.ProcessorTestCase<?> testCase =
                ProcessorTestHelper.getTestCaseStream()
                        .filter(tc -> tc.ruleKey.equals("2755"))
                        .findFirst()
                        .get();
        File statsFile = tempDir.toPath().resolve("stats.json").toFile();

        ProcessorTestHelper.runSorald(
                testCase, Constants.ARG_STATS_OUTPUT_FILE, statsFile.getAbsolutePath());

        JSONObject jo = FileUtils.readJSON(statsFile.toPath());
        JSONArray repairs = jo.getJSONArray(StatsMetadataKeys.REPAIRS);
        assertThat(repairs.length(), greaterThan(0));

        for (int i = 0; i < repairs.length(); i++) {
            assertThat(
                    repairs.getJSONObject(i).keySet(),
                    containsInAnyOrder(
                            StatsMetadataKeys.REPAIR_RULE_KEY,
                            StatsMetadataKeys.REPAIR_RULE_NAME,
                            StatsMetadataKeys.REPAIR_CRASHED_LOCATIONS,
                            StatsMetadataKeys.REPAIR_PERFORMED_LOCATIONS,
                            StatsMetadataKeys.REPAIR_NB_PERFORMED,
                            StatsMetadataKeys.REPAIR_NB_FAILURES,
                            StatsMetadataKeys.REPAIR_NB_VIOLATIONS_BEFORE,
                            StatsMetadataKeys.REPAIR_NB_VIOLATIONS_AFTER));
        }

        assertThat(
                jo.getJSONObject(StatsMetadataKeys.EXECUTION_INFO)
                        .getJSONArray(StatsMetadataKeys.ORIGINAL_ARGS)
                        .length(),
                greaterThan(0));

        assertThat(jo.getLong(StatsMetadataKeys.PARSE_TIME_MS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.REPAIR_TIME_MS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.START_TIME_MS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.END_TIME_MS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.TOTAL_TIME_MS), greaterThan(0L));
    }

    /** Check that the amount of violations is correct when using targeted repair. */
    @Test
    public void statisticsFile_containsCorrectNbViolationsBeforeAndAfter_whenUsingTargetedRepair(
            @TempDir File tmpDir) throws IOException {
        // arrange
        File statsFile = tmpDir.toPath().resolve("stats.json").toFile();
        File processorTestFiles = ProcessorTestHelper.TEST_FILES_ROOT.toFile();
        File project = tmpDir.toPath().resolve("project").toFile();
        org.apache.commons.io.FileUtils.copyDirectory(processorTestFiles, project);

        SoraldAbstractProcessor<?> targetProc = new XxeProcessingProcessor();
        JavaFileScanner targetCheck = Checks.getCheckInstance(targetProc.getRuleKey());

        Set<RuleViolation> viloationsBefore =
                ProjectScanner.scanProject(project, project, targetCheck);
        RuleViolation targetViolation =
                new ArrayList<>(viloationsBefore).get(viloationsBefore.size() / 2);
        String specifier = targetViolation.relativeSpecifier(project.toPath());

        // act
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    project.getAbsolutePath(),
                    Constants.ARG_STATS_OUTPUT_FILE,
                    statsFile.getAbsolutePath(),
                    Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.IN_PLACE.name(),
                    Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                    specifier
                });

        // assert
        Set<RuleViolation> violationsAfter =
                ProjectScanner.scanProject(project, project, targetCheck);
        assertThat(
                "Targeted repair did not do its job...",
                violationsAfter.size(),
                equalTo(viloationsBefore.size() - 1));

        JSONObject jo = FileUtils.readJSON(statsFile.toPath());

        JSONArray repairStats = jo.getJSONArray(StatsMetadataKeys.REPAIRS);
        assertThat("unexpected amount of repair stats", repairStats.length(), equalTo(1));

        JSONObject xxeRepairStats = repairStats.getJSONObject(0);
        assertThat(
                xxeRepairStats.getInt(StatsMetadataKeys.REPAIR_NB_VIOLATIONS_BEFORE),
                equalTo(viloationsBefore.size()));
        assertThat(
                xxeRepairStats.getInt(StatsMetadataKeys.REPAIR_NB_VIOLATIONS_AFTER),
                equalTo(violationsAfter.size()));
    }

    @Test
    public void segmentRepair_doesNotSupportStatsCollection(@TempDir File tempDir) {
        File statsFile = tempDir.toPath().resolve("stats.json").toFile();
        String[] cliArgs =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    ProcessorTestHelper.TEST_FILES_ROOT.toString(),
                    Constants.ARG_RULE_KEYS,
                    "2755",
                    Constants.ARG_REPAIR_STRATEGY,
                    RepairStrategy.SEGMENT.name(),
                    Constants.ARG_STATS_OUTPUT_FILE,
                    statsFile.getAbsolutePath()
                };

        assertThrows(SystemExitHandler.NonZeroExit.class, () -> Main.main(cliArgs));
    }
}
