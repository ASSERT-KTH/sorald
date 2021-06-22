import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class OneClosedOneUnclosed {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
