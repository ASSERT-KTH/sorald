/*
When the processor inlines a local variable declaration into the resources block, it also cleans
up referenses to said variable in the catchers and finalizer. This test case includes a field
with the same name as the inlined variable, and is meant to verify that we don't accidentally
delete references to this unrelated field.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReferenceInCatcherAndFinalizerToFieldWithSameNameAsResource {
    private int bw = 2;

    public void saveTo(File file) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file)); // Noncompliant
            bw.write("Write some stuff to file");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(this.bw); // reference to field; should not be cleaned!
            if (bw != null) { // reference to local variable; should be cleaned!
                bw.close();
            }
        } finally {
            System.out.println(this.bw); // reference to field; should not be cleaned!
            System.out.println("done");
        }
    }
}