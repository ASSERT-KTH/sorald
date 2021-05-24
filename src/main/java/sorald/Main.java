package sorald;

import sorald.cli.Cli;

public class Main {
    public static void main(String[] args) {
        try {
            throw new InterruptedException();
        } catch (InterruptedException | IllegalArgumentException e) {
            System.out.println("oops!");
        }

        int exitStatus = Cli.createCli().execute(args);
        if (exitStatus != 0) {
            int otherExitStatus = exitStatus;
            System.exit(exitStatus);
        }
    }
}
