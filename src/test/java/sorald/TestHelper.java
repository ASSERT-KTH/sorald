package sorald;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

public class TestHelper {

    /*
    Simple helper method that removes the mandatory // Noncompliant comments from test files.
     */
    public static void removeComplianceComments(String pathToRepairedFile) {
        final String complianceComment = "// Noncompliant";
        try {
            BufferedReader file = new BufferedReader(new FileReader(pathToRepairedFile));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                if (line.contains(complianceComment)) {
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
