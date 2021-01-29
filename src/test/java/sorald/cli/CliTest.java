package sorald.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sorald.Constants;
import sorald.FileOutputStrategy;
import sorald.RepairStrategy;

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

    /**
     * Output strategies ALL and CHANGED_ONLY are incompatible with repair strategy MAVEN for
     * multi-rule repair, and the CLI should stop execution early.
     */
    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CHANGED_ONLY"})
    public void repairStrategyMAVEN_cantDoMultiRuleRepair_withOutputStrategies_ALL_and_CHANGED_ONLY(
            String outputStrategy) {
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));

        int exitStatus =
                Cli.createCli()
                        .execute(
                                Constants.REPAIR_COMMAND_NAME,
                                Constants.ARG_FILE_OUTPUT_STRATEGY,
                                outputStrategy,
                                Constants.ARG_ORIGINAL_FILES_PATH,
                                Constants.PATH_TO_RESOURCES_FOLDER,
                                Constants.ARG_REPAIR_STRATEGY,
                                RepairStrategy.MAVEN.name(),
                                Constants.ARG_RULE_KEYS,
                                "2111,2184");

        assertThat(exitStatus, not(equalTo(0)));
        assertThat(
                err.toString(),
                containsString(
                        String.format(
                                "MAVEN can only be used with %s=%s for multi-rule repair",
                                Constants.ARG_FILE_OUTPUT_STRATEGY,
                                FileOutputStrategy.IN_PLACE.name())));
    }
}
