package sorald.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine;

/** Class containing the CLI for Sorald. */
public class Cli {

    /** @return Sorald's command line interface. */
    public static CommandLine createCli() {
        return new CommandLine(new SoraldCLI());
    }

    @CommandLine.Command(
            name = "sorald",
            mixinStandardHelpOptions = true,
            subcommands = {RepairCommand.class, MineCommand.class},
            description =
                    "The Sorald command line application for automatic repair of Sonar rule violations.",
            synopsisSubcommandLabel = "<COMMAND>",
            versionProvider = SoraldVersionProvider.class)
    static class SoraldCLI implements Callable<Integer> {

        @Override
        public Integer call() {
            new CommandLine(this).usage(System.out);
            return -1;
        }
    }
}
