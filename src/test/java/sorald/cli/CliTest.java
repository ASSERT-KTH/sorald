package sorald.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

/** General tests of the CLI functionality. */
public class CliTest {

    @Test
    public void cli_exitsNonZero_whenExecutedWithoutSubcommand() {
        int exitStatus = Cli.createCli().execute();
        assert exitStatus != 0;
    }

    @Test
    public void cli_providesLocalVersion_whenNotPackaged() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int exitStatus = Cli.createCli().execute("--version");

        assertThat(exitStatus, equalTo(0));
        assertThat(out.toString(), containsString(SoraldVersionProvider.LOCAL_VERSION));
    }
}
