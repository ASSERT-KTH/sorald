import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String[] mvnVersion = {"mvn", "--version"};
        String[] mvnCmdVersion = {"mvn.cmd", "-version"};
        String[] mvnAbsPathVersion = {"C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-3.8.3\\bin\\mvn.cmd", "-version"};

        runProcess(mvnVersion);
        runProcess(mvnCmdVersion);
        runProcess(mvnAbsPathVersion);
    }

    public static void runProcess(String[] cmd) {
        System.out.println("Executing " + Arrays.toString(cmd));

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            // No STDERR => merge to STDOUT
            pb.redirectErrorStream(true);

            Process p = pb.start();

            try(var stdo = p.getInputStream()) {
                stdo.transferTo(System.out);
            }

            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
