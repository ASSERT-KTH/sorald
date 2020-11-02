package sorald;

import java.io.File;
import java.nio.file.Paths;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Helper class the cleans up the default sorald workspace, if it exists, before each test executes.
 * This is to prevent tests using the default workspace from interfering with each other.
 */
public class WorkspaceCleaner implements TestExecutionListener {

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        File workspace =
                Paths.get(Constants.SORALD_WORKSPACE).toAbsolutePath().normalize().toFile();
        if (workspace.exists()) {
            FileUtils.deleteDirectory(workspace);
        }
    }
}
