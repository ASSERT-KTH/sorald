package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

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
        File statsFile =
                new File(Constants.SORALD_WORKSPACE).toPath().resolve("stats.json").toFile();
        List<String> cliArgs =
                List.of(
                        Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                                ProcessorTestHelper.TEST_FILES_ROOT.toString(),
                        Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS, "2755",
                        Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
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
                testCase,
                Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
                statsFile.getAbsolutePath());

        JSONObject jo = FileUtils.readJSON(statsFile.toPath());
        JSONArray repairs = jo.getJSONArray(StatsMetadataKeys.REPAIRS);
        assertThat(repairs.length(), greaterThan(0));

        for (int i = 0; i < repairs.length(); i++) {
            assertThat(
                    repairs.getJSONObject(i).keySet(),
                    containsInAnyOrder(
                            StatsMetadataKeys.REPAIR_RULE_KEY,
                            StatsMetadataKeys.REPAIR_RULE_VIOLATION_POSITION));
        }

        assertThat(
                jo.getJSONArray(StatsMetadataKeys.ORIGINAL_ARGS).toList().size(), greaterThan(0));
        assertThat(jo.getLong(StatsMetadataKeys.PARSE_TIME_NS), greaterThan(0L));
        assertThat(jo.getLong(StatsMetadataKeys.REPAIR_TIME_NS), greaterThan(0L));
    }
}
