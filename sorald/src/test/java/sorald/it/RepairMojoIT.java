package sorald.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.io.FileMatchers.anExistingFile;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import sorald.FileUtils;
import sorald.event.StatsMetadataKeys;

@MavenJupiterExtension
public class RepairMojoIT {
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenTest
    @DisplayName("Repair fails when ruleKey is not passed")
    void fails_without_ruleKey_parameter(MavenExecutionResult result) {
        assertThat(result)
                .isFailure()
                .out()
                .plain()
                .asString()
                .contains(
                        "Caused by: org.apache.maven.plugin.PluginParameterException: The parameters 'rules'");
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenOption("-DruleKey=S1068")
    @MavenTest
    @DisplayName("Repair works on an empty Maven project")
    void empty_project(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenOption("-DruleKey=S1217")
    @MavenTest
    @DisplayName("Successfully exit when there are no violations")
    void do_nothing_when_there_are_no_violations(MavenExecutionResult result) {
        assertThat(result)
                .isSuccessful()
                .out()
                .plain()
                .contains("No rule violations found, nothing to do ...");
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenOption("-DruleKey=S2225")
    @MavenTest
    @DisplayName("Perform repairs when a violation is found")
    void perform_repairs(MavenExecutionResult result) {
        assertThat(result)
                .isSuccessful()
                .out()
                .plain()
                .contains(
                        "-----Number of fixes------",
                        "ToStringReturningNullProcessor: 1",
                        "-----End of report------");
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenOption("-DruleKey=S2111")
    @MavenOption("-DstatsOutputFile=stats.json")
    @MavenTest
    @DisplayName("Repair respects stats output file parameter and generates a JSON file")
    void stats_output_file(MavenExecutionResult result) throws IOException {
        File projectRoot = result.getMavenProjectResult().getTargetProjectDirectory();
        File statsOutputFile = new File(projectRoot, "stats.json");

        org.hamcrest.MatcherAssert.assertThat(statsOutputFile, anExistingFile());

        Path expectedOutputFile =
                Paths.get(
                        "target/maven-it/sorald/it/RepairMojoIT/stats_output_file/project/src/test/resources/expected-repairs.json");
        JSONObject expectedJsonObject = FileUtils.readJSON(expectedOutputFile);
        JSONObject actualJsonObject = FileUtils.readJSON(statsOutputFile.toPath());

        JSONArray expectedRepairs = expectedJsonObject.getJSONArray(StatsMetadataKeys.REPAIRS);
        JSONArray actualRepairs = actualJsonObject.getJSONArray(StatsMetadataKeys.REPAIRS);

        org.hamcrest.MatcherAssert.assertThat(
                ((JSONObject) actualRepairs.get(0)).toMap(),
                equalTo(((JSONObject) expectedRepairs.get(0)).toMap()));
    }
}
