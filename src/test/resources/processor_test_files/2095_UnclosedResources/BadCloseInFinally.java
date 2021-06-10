/*
Sometimes there is an "insufficient" close in the finalizer. In this example, bw1.close() might
throw, so bw2.close() may not execute. In such cases, Sonar flags the bw2 initialization as an
unclosed resource, but when we inline that into a resources block we must also make sure to clean
out any references to the variable in the finalizer.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BadCloseInFinally {

    public static void saveTo(File file1, File file2) {
        BufferedWriter bw1 = null;
        BufferedWriter bw2 = null;
        try {
            bw1 = new BufferedWriter(new FileWriter(file1));
            bw1.write("Write some stuff to file 1");

            bw2 = new BufferedWriter(new FileWriter(file2)); // Noncompliant
            bw2.write("Write some stuff to file 2");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("done");
            try {
                if (bw1 != null) {
                    bw1.close();
                }
                if (bw2 != null) {
                    bw2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}