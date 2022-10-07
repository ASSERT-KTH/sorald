package sorald.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.junit.jupiter.api.DisplayName;

@MavenJupiterExtension
public class RepairMojoIT {
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:repair")
    @MavenTest
    @DisplayName("Repair fails when ruleKey is not passed")
    void fails_without_ruleKey_parameter(MavenExecutionResult result) {
        assertThat(result).isFailure();
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
}
