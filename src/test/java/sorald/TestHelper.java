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
        org.apache.commons.io.FileUtils.copyDirectory(
                PATH_TO_RESOURCES_FOLDER.toFile(), tempDir.toFile());
        tempDir.toFile().deleteOnExit();
        return tempDir;
    }

    /**
     * @return path to a new workspace filled with the processor test files.
     * @throws IOException If creating the workspace is unsuccessful.
     */
    public static Path createTemporaryProcessorTestFilesWorkspace() throws IOException {
        return createTemporaryTestResourceWorkspace().resolve("processor_test_files");
    }
}
