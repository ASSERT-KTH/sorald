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
import org.sonar.java.checks.DeadStoreCheck;
import sorald.event.StatsMetadataKeys;
import sorald.sonar.RuleVerifier;

public class MavenLauncherTest {

    @Test
    public void sorald_repairsProductionAndTestCode_inMavenProject(@TempDir File workdir)
            throws IOException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("scenario_test_files/simple-java8-maven-project")
                        .toFile(),
                workdir);

        Path productionFile = workdir.toPath().resolve("src/main/java/sorald/test/App.java");
        Path testFile = workdir.toPath().resolve("src/test/java/sorald/test/AppTest.java");

        RuleVerifier.verifyHasIssue(productionFile.toString(), new DeadStoreCheck());
        RuleVerifier.verifyHasIssue(testFile.toString(), new DeadStoreCheck());

        String[] args =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SOURCE,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_KEY,
                    "1854",
                    Constants.ARG_REPAIR_STRATEGY,
                    RepairStrategy.MAVEN.name()
                };

        // act
        Main.main(args);

        // assert
        RuleVerifier.verifyNoIssue(productionFile.toString(), new DeadStoreCheck());
        RuleVerifier.verifyNoIssue(testFile.toString(), new DeadStoreCheck());
    }

    /**
     * Test that Sorald can repair a rule violation for which Sonar requires the full classpath to
     * detect.
     */
    @Test
    void sorald_repairsRuleViolation_thatRequiresClasspathToDetect(@TempDir File workdir)
            throws IOException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("scenario_test_files")
                        .resolve("classpath-dependent-project")
                        .toFile(),
                workdir);

        Path statsFile = workdir.toPath().resolve("stats.json");

        String castArithmOperandKey = "2184";
        String[] args = {
            Constants.REPAIR_COMMAND_NAME,
            Constants.ARG_SOURCE,
            workdir.getAbsolutePath(),
            Constants.ARG_RULE_KEY,
            castArithmOperandKey,
            Constants.ARG_REPAIR_STRATEGY,
            RepairStrategy.MAVEN.name(),
            Constants.ARG_STATS_OUTPUT_FILE,
            statsFile.toString()
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
