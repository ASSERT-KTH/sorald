package sorald;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    public static final Path PATH_TO_RESOURCES_FOLDER =
            Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    public static final String SORALD_WORKSPACE = "sorald-workspace";

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
}
