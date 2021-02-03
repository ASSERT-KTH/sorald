package sorald;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.java.checks.DeadStoreCheck;
import sorald.sonar.RuleVerifier;

public class MavenLauncherTest {

    @Test
    public void sorald_repairsProductionAndTestCode_inMavenProject(@TempDir File workdir)
            throws IOException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
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
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_KEYS,
                    "1854",
                    Constants.ARG_FILE_OUTPUT_STRATEGY,
                    FileOutputStrategy.IN_PLACE.name(),
                    Constants.ARG_REPAIR_STRATEGY,
                    RepairStrategy.MAVEN.name()
                };

        // act
        Main.main(args);

        // assert
        RuleVerifier.verifyNoIssue(productionFile.toString(), new DeadStoreCheck());
        RuleVerifier.verifyNoIssue(testFile.toString(), new DeadStoreCheck());
    }
}
