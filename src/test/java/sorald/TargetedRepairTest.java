package sorald;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;

/** Tests for the targeted repair functionality of Sorald. */
public class TargetedRepairTest {

    @Test
    void targetedRepair_correctlyRepairsSingleViolation(@TempDir File workdir) throws Exception {
        // arrange
        TargetedRepairWorkdirInfo workdirInfo = setupWorkdir(workdir);

        // act
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                    workdirInfo.targetViolation.relativeSpecifier(workdir.toPath())
                });

        // assert
        File soraldWorkspace = new File(Constants.SORALD_WORKSPACE);
        List<File> repairedFiles =
                FileUtils.findFilesByExtension(soraldWorkspace, Constants.JAVA_EXT);
        Set<RuleViolation> violationsAfter =
                ProjectScanner.scanProject(soraldWorkspace, soraldWorkspace, workdirInfo.check);

        assertThat(violationsAfter.size(), equalTo(workdirInfo.numViolationsBefore - 1));
        assertFalse(violationsAfter.contains(workdirInfo.targetViolation));
        assertThat(repairedFiles.size(), equalTo(1));
        assertThat(repairedFiles.get(0).getName(), equalTo(workdirInfo.targetFile.getName()));
    }

    /** It should not be possible to specify both rule keys and specific rule violations. */
    @Test
    public void targetedRepair_cannotBeUsedWithRuleKeys(@TempDir File workdir) throws Exception {
        // arrange
        TargetedRepairWorkdirInfo workdirInfo = setupWorkdir(workdir);
        var args =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                    workdirInfo.targetViolation.relativeSpecifier(workdir.toPath()),
                    Constants.ARG_RULE_KEYS,
                    "2755"
                };

        // act/assert
        assertThrows(SystemExitHandler.NonZeroExit.class, () -> Main.main(args));
    }

    @Test
    public void targetedRepair_requiresViolationPath_existsInOriginalFilesPath(
            @TempDir File workdir) throws Exception {
        // arrange
        TargetedRepairWorkdirInfo workdirInfo = setupWorkdir(workdir);

        // make the violation ID relative to some other directory
        String badViolationId =
                workdirInfo.targetViolation.relativeSpecifier(workdir.getParentFile().toPath());
        var args =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                    badViolationId
                };

        // act/assert
        assertThrows(SystemExitHandler.NonZeroExit.class, () -> Main.main(args));
    }

    @Test
    public void targetedRepair_acceptsAbsoluteViolationPath(@TempDir File workdir)
            throws Exception {
        // arrange
        TargetedRepairWorkdirInfo workdirInfo = setupWorkdir(workdir);

        // act
        Path rootDir = workdir.toPath().toAbsolutePath().getRoot();
        String absoluteViolationId =
                workdirInfo
                        .targetViolation
                        .relativeSpecifier(rootDir)
                        .replaceFirst(
                                Constants.VIOLATION_SPECIFIER_SEP, File.pathSeparator + rootDir);
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                    absoluteViolationId
                });

        // assert
        File soraldWorkspace = new File(Constants.SORALD_WORKSPACE);
        Set<RuleViolation> violationsAfter =
                ProjectScanner.scanProject(soraldWorkspace, soraldWorkspace, workdirInfo.check);
        assertThat(violationsAfter.size(), equalTo(workdirInfo.numViolationsBefore - 1));
    }

    /** Setup the workdir with a specific target violation. */
    private static TargetedRepairWorkdirInfo setupWorkdir(File workdir) throws IOException {
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

        List<RuleViolation> violationsBefore =
                new ArrayList<>(ProjectScanner.scanProject(targetFile, workdir, check));
        assertThat(
                "there must be more than 1 violation in the test file for an adequate test",
                violationsBefore.size(),
                greaterThan(1));
        violationsBefore.sort(RuleViolation::compareTo);
        RuleViolation violation = violationsBefore.get(violationsBefore.size() / 2);

        return new TargetedRepairWorkdirInfo(check, violationsBefore.size(), targetFile, violation);
    }

    /** Simple container for info about the targeted repair working directory. */
    private static class TargetedRepairWorkdirInfo {
        final JavaFileScanner check;
        final int numViolationsBefore;
        final File targetFile;
        final RuleViolation targetViolation;

        private TargetedRepairWorkdirInfo(
                JavaFileScanner check,
                int numViolationsBefore,
                File targetFile,
                RuleViolation targetViolation) {
            this.check = check;
            this.numViolationsBefore = numViolationsBefore;
            this.targetFile = targetFile;
            this.targetViolation = targetViolation;
        }
    }
}
