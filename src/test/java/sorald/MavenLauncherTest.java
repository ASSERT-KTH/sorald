package sorald;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MavenLauncherTest {

    @Test
    public void sorald_canRepairMavenProject(@TempDir File workdir) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("scenario_test_files/simple-java8-maven-project")
                        .toFile(),
                workdir);

        String[] args =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    workdir.getAbsolutePath(),
                    Constants.ARG_RULE_KEYS,
                    "1854",
                    Constants.ARG_MAVEN
                };

        Main.main(args);
    }
}
