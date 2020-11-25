package sorald.cli;

import org.junit.jupiter.api.Test;

/** General tests of the CLI functionality. */
public class CliTest {

    @Test
    public void cli_exitsNonZero_whenExecutedWithoutSubcommand() {
        int exitStatus = Cli.createCli().execute();
        assert exitStatus != 0;
    }
}
