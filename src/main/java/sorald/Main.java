package sorald;

import sorald.cli.Cli;

public class Main {
    public static void main(String[] args) throws Exception {
        int exitStatus = Cli.createCli().execute(args);

        // TODO change this behavior to just exit with the status, we don't actually want to throw
        //  an exception in the CLI
        if (exitStatus != 0) {
            throw new IllegalStateException("CLI exited non-zero");
        }
    }
}
