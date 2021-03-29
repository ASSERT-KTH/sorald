package sorald;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    public static final Path PATH_TO_RESOURCES_FOLDER =
            Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    public static final String SORALD_WORKSPACE = "sorald-workspace";

    /**
     * On macOS, temporary directories are put in /var, which is just a symlink to /private/var. To
     * reproduce this on non-macOS systems, set the below environment variable to true, e.g. <code>
     * export SORALD_TEST_SYMLINK_WORKSPACE=true</code> on Linux.
     */
    public static final boolean USE_SYMLINKED_WORKSPACE =
            Boolean.parseBoolean(System.getenv("SORALD_TEST_SYMLINK_WORKSPACE"));

    /**
     * Simple helper method that removes the mandatory // Noncompliant comments from test files,
     * except for lines that contain NOSONAR comments.
     */
    public static void removeComplianceComments(String pathToRepairedFile) {
        final String complianceComment = "// Noncompliant";
        final String noSonar = "NOSONAR";
        try {
            BufferedReader file = new BufferedReader(new FileReader(pathToRepairedFile));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                if (line.contains(complianceComment) && !line.contains(noSonar)) {
                    line.trim();
                    line = line.substring(0, line.indexOf(complianceComment));
                }
                inputBuffer.append(line + '\n');
            }
            file.close();
            FileOutputStream fileOut = new FileOutputStream(pathToRepairedFile);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
    }

    /**
     * @return path to a new workspace filled with all test resources.
     * @throws IOException If creating the workspace is unsuccessful.
     */
    public static Path createTemporaryTestResourceWorkspace() throws IOException {
        Path tempDir = Files.createTempDirectory("sorald-test-workspace");
        tempDir.toFile().deleteOnExit();

        Path workdir = tempDir.resolve("tempdir");
        workdir.toFile().mkdir();

        org.apache.commons.io.FileUtils.copyDirectory(
                PATH_TO_RESOURCES_FOLDER.toFile(), workdir.toFile());

        if (USE_SYMLINKED_WORKSPACE) {
            // Somewhat reproduce macOS environment with a symlinked workspace
            Path symlinkDir = tempDir.resolve("symlinks");
            symlinkDir.toFile().mkdir();
            Path symlinkToWorkdir = symlinkDir.resolve("workdir");
            Files.createSymbolicLink(symlinkToWorkdir, workdir);
            return symlinkToWorkdir;
        } else {
            return workdir;
        }
    }

    /**
     * @return path to a new workspace filled with the processor test files.
     * @throws IOException If creating the workspace is unsuccessful.
     */
    public static Path createTemporaryProcessorTestFilesWorkspace() throws IOException {
        return createTemporaryTestResourceWorkspace().resolve("processor_test_files");
    }
}
