package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sorald.event.StatsMetadataKeys;
import sorald.processor.ProcessorTestHelper;

public class GatherStatsTest {
    @Test
    public void statisticsFile_containsOriginalArgs(@TempDir File tempDir) throws Exception {
        File statsFile = tempDir.toPath().resolve("stats.json").toFile();
        List<String> cliArgs =
                List.of(
                        Constants.REPAIR_COMMAND_NAME,
                        Constants.ARG_ORIGINAL_FILES_PATH,
                        ProcessorTestHelper.TEST_FILES_ROOT.toString(),
                        Constants.ARG_RULE_KEYS,
                        "2755",
                        Constants.ARG_STATS_OUTPUT_FILE,
                        statsFile.getAbsolutePath());
        Main.main(cliArgs.toArray(String[]::new));

        JSONObject jo = FileUtils.readJSON(statsFile.toPath());
        JSONArray args = jo.getJSONArray(StatsMetadataKeys.ORIGINAL_ARGS);

        assertThat(args.toList(), equalTo(cliArgs));
    }

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
                            StatsMetadataKeys.REPAIR_PERFORMED_LOCATIONS));
        }

        assertThat(
                jo.getJSONObject(StatsMetadataKeys.EXECUTION_INFO)
                        .getJSONArray(StatsMetadataKeys.ORIGINAL_ARGS)
                        .length(),
                greaterThan(0));
        assertThat(jo.getLong(StatsMetadataKeys.PARSE_TIME_NS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.REPAIR_TIME_NS), greaterThan(0L));
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
