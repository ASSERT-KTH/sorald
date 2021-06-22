/*
Tests that the processor can handle multiple closeables in the same try, when none of them are
closed initially.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MultipleCloseablesInSingleTry {
    public void readAndWrite() {

        try {
            FileInputStream is = new FileInputStream(new File("random/file/path")); // Noncompliant
            FileOutputStream os = new FileOutputStream(new File("some/other/file")); // Noncompliant

            byte[] bytes = is.readAllBytes();
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}