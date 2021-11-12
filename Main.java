

public class Main {
    public static void main(String[] args) throws Exception {
        String[] mvnVersion = {"mvn", "--version"};
        String[] mvnCmdVersion = {"mvn.cmd", "-version"};

        runProcess(mvnVersion);
        runProcess(mvnCmdVersion);
    }

    public static void runProcess(String[] cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        // No STDERR => merge to STDOUT
        pb.redirectErrorStream(true);

        Process p = pb.start();


        try(var stdo = p.getInputStream()) {
            stdo.transferTo(System.out);
        }

        p.waitFor();
    }
}
