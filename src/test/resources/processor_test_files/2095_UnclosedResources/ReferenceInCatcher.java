/*
When an unclosed resource is referenced in a catcher, we must ensure that those references are
removed when the resource is inlined into the resource list.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReferenceInCatcher {

    public static void saveTo(File file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file)); // Noncompliant
            bw.write("Write some stuff to file");
        } catch (IOException e) {
            e.printStackTrace();
            if (bw != null) {
                bw.close();
            }
        } finally {
            System.out.println("done");
        }
    }
}
