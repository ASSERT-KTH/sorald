package sorald;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import sorald.sonar.RuleVerifier;

public class MultipleProcessorsTest {

    @ParameterizedTest
    @EnumSource(RepairStrategy.class)
    public void allStrategies_canApplyMultipleProcessors(
            RepairStrategy repairStrategy, @TempDir File tempDir) throws IOException {
        File origBuggyFile =
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("MultipleProcessors.java")
                        .toFile();
        File buggyFile = tempDir.toPath().resolve(origBuggyFile.getName()).toFile();
        org.apache.commons.io.FileUtils.copyFile(origBuggyFile, buggyFile);

        String pathToRepairedFile = buggyFile.getAbsolutePath();
        String originalFilesPath = buggyFile.getAbsolutePath();
        if (repairStrategy == RepairStrategy.MAVEN) {
            MavenHelper.convertToMavenProject(tempDir);
            originalFilesPath = tempDir.getAbsolutePath();
            pathToRepairedFile =
                    FileUtils.findFilesByExtension(tempDir, Constants.JAVA_EXT)
                            .get(0)
                            .getAbsolutePath();
        }

        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    originalFilesPath,
                    Constants.ARG_RULE_KEYS,
                    "2111,2184,2204",
                    Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE,
                    Constants.ARG_REPAIR_STRATEGY,
                    repairStrategy.name(),
                    Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.IN_PLACE.name()
                });
        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new EqualsOnAtomicClassCheck());
    }
}
