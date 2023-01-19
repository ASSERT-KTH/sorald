package sorald.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static org.hamcrest.io.FileMatchers.anExistingFile;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.DisplayName;

@MavenJupiterExtension
public class MineMojoIT {
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:mine")
    @MavenTest
    @DisplayName("Mine works on an empty Maven project")
    void empty_project(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
    }

    @MavenTest
    @DisplayName("Mine works when configured in the POM file")
    void pom_configured(MavenExecutionResult result) throws IOException {
        Path expectedOutputFile =
                Paths.get(
                        "target/maven-it/sorald/it/MineMojoIT/pom_configured/project/src/test/resources/expected-output.txt");
        List<String> expectedOutput = Files.readAllLines(expectedOutputFile);

        assertThat(result)
                .isSuccessful()
                .out()
                .plain()
                .contains(expectedOutput.toArray(new String[0]));
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:mine")
    @MavenTest
    @DisplayName("Mine works on a non-empty Maven project")
    void mine_for_violations(MavenExecutionResult result) throws IOException {
        Path expectedOutputFile =
                Paths.get(
                        "target/maven-it/sorald/it/MineMojoIT/mine_for_violations/project/src/test/resources/expected-output.txt");
        List<String> expectedOutput = Files.readAllLines(expectedOutputFile);

        assertThat(result)
                .isSuccessful()
                .out()
                .plain()
                .contains(expectedOutput.toArray(new String[0]));
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:mine")
    @MavenOption("-DhandledRules")
    @MavenTest
    @DisplayName("Mine respects handled rules parameter")
    void handled_rules(MavenExecutionResult result) throws IOException {
        Path expectedOutputFile =
                Paths.get(
                        "target/maven-it/sorald/it/MineMojoIT/handled_rules/project/src/test/resources/expected-output.txt");
        List<String> expectedOutput = Files.readAllLines(expectedOutputFile);

        assertThat(result)
                .isSuccessful()
                .out()
                .plain()
                .contains(expectedOutput.toArray(new String[0]));
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:mine")
    @MavenOption("-DhandledRules")
    @MavenOption("-DstatsOutputFile=stats.json")
    @MavenTest
    @DisplayName("Mine respects stats output file parameter and generates a JSON file")
    void stats_output_file(MavenExecutionResult result) {
        Path projectRoot = result.getMavenProjectResult().getTargetProjectDirectory();
        File statsOutputFile = new File(projectRoot.toFile(), "stats.json");

        org.hamcrest.MatcherAssert.assertThat(statsOutputFile, anExistingFile());
    }
}
