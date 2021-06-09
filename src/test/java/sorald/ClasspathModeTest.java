package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sorald.event.StatsMetadataKeys;

/** Tests for running Sorald in classpath mode. */
class ClasspathModeTest {

    @Test
    void resolveClasspathFrom_enablesRepairOfViolation_thatRequiresClasspathToDetect(
            @TempDir File workdir) throws IOException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("scenario_test_files")
                        .resolve("classpath-dependent-project")
                        .toFile(),
                workdir);

        Path statsFile = workdir.toPath().resolve("stats.json");
        Path source = workdir.toPath().resolve("src").resolve("main").resolve("java");

        String castArithmOperandKey = "2184";
        String[] args = {
            Constants.REPAIR_COMMAND_NAME,
            Constants.ARG_SOURCE,
            source.toString(),
            Constants.ARG_RULE_KEY,
            castArithmOperandKey,
            Constants.ARG_STATS_OUTPUT_FILE,
            statsFile.toString(),
            Constants.ARG_RESOLVE_CLASSPATH_FROM,
            workdir.getAbsolutePath()
        };

        // act
        Main.main(args);

        // assert
        JSONObject stats = FileUtils.readJSON(statsFile);
        JSONArray repairs = stats.getJSONArray(StatsMetadataKeys.REPAIRS);
        assertThat(repairs.length(), equalTo(1));
        JSONObject repair = repairs.getJSONObject(0);
        assertThat(
                repair.getString(StatsMetadataKeys.REPAIR_RULE_KEY), equalTo(castArithmOperandKey));
    }
}
