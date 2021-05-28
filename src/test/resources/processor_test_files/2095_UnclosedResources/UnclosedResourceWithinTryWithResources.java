/*
Tests that the processor can handle an unclosed resource within a try-with-resources.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MultipleCloseablesInSingleTry {
    public void readAndWrite() {

        try (FileInputStream is = new FileInputStream(new File("random/file/path"))) {
            FileOutputStream os = new FileOutputStream(new File("some/other/file")); // Noncompliant

            byte[] bytes = is.readAllBytes();
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
