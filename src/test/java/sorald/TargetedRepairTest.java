package sorald;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Tests for the targeted repair functionality of Sorald. */
public class TargetedRepairTest {

    @Test
    void targetedRepair_correctlyRepairsSingleViolation(@TempDir File workdir) throws Exception {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                new File(Constants.PATH_TO_RESOURCES_FOLDER), workdir);

        String ruleKey = "2111";
        JavaFileScanner check = Checks.getCheckInstance(ruleKey);
        File targetFile =
                workdir.toPath()
                        .resolve("processor_test_files")
                        .resolve("2111_BigDecimalDoubleConstructor")
                        .resolve("BigDecimalDoubleConstructor.java")
                        .toFile();

        Set<RuleViolation> violationsBefore =
                ProjectScanner.scanProject(targetFile, workdir, check);
        assertThat(
                "there must be more than 1 violation in the test file for an adequate test",
                violationsBefore.size(),
                greaterThan(1));
        RuleViolation targetViolation =
                violationsBefore.stream()
                        .sorted(Comparator.comparing(RuleViolation::getLineNumber))
                        .findFirst()
                        .get();

        // act
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_VIOLATIONS,
                    String.format(
                            "%s:%s:%s",
                            ruleKey, targetViolation.getFileName(), targetViolation.getLineNumber())
                });

        // assert
        File soraldWorkspace = new File(Constants.SORALD_WORKSPACE);
        List<File> repairedFiles =
                FileUtils.findFilesByExtension(soraldWorkspace, Constants.JAVA_EXT);
        Set<RuleViolation> violationsAfter =
                ProjectScanner.scanProject(soraldWorkspace, soraldWorkspace, check);

        assertThat(violationsAfter.size(), equalTo(violationsBefore.size() - 1));
        assertFalse(violationsAfter.contains(targetViolation));
        assertThat(repairedFiles.size(), equalTo(1));
        assertThat(repairedFiles.get(0).getName(), equalTo(targetFile.getName()));
    }
}
