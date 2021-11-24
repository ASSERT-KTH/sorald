import java.io.File;

public class Main {
    public static void main(String[] args) {
        for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
            File file = new File(dirname, "mvn");
            if (file.isFile() && file.canExecute()) {
                System.out.println(file.getAbsolutePath());
            }
        }
    }
}