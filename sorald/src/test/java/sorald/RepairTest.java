package sorald;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.sonar.ProjectScanner;
import sorald.sonar.SonarRule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepairTest {

    /** The CLI should refuse multiple rules used in violation specifiers. */
    @Test
    public void repair_doesNotAllowMultipleRules(@TempDir File workdir) throws IOException {
        // arrange
        File origFile =
                TestHelper.PATH_TO_RESOURCES_FOLDER.resolve("MultipleProcessors.java").toFile();
        File targetFile = workdir.toPath().resolve(origFile.getName()).toFile();
        org.apache.commons.io.FileUtils.copyFile(origFile, targetFile);
        SoraldConfig config = new SoraldConfig();

        List<Rule> sonarRules =
                Stream.of("S2111", "S2184").map(SonarRule::new).collect(Collectors.toList());
        Set<RuleViolation> violations = ProjectScanner.scanProject(targetFile, workdir, sonarRules);

        // act
        var repair = new Repair(config, List.of(), List.of());
        try {
            repair.repair(violations);
        } catch (IllegalArgumentException e) {
            // assert
            String msg = e.getMessage();
            assertThat(msg, containsString("expected rule violations for precisely 1 rule key"));
        }
    }
}
