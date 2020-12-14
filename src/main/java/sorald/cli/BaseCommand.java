package sorald.cli;

import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sorald.Constants;

/** Base command containing the options in common for all Sorald subcommands. */
@CommandLine.Command()
abstract class BaseCommand implements Callable<Integer> {
    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(
            names = Constants.ARG_TARGET,
            description =
                    "The target of this execution (ex. sorald/92d377). This will be included in the json report.")
    String target;

    @CommandLine.Option(
            names = Constants.ARG_STATS_OUTPUT_FILE,
            description =
                    "Path to a file to store execution statistics in (in JSON format). If left unspecified, Sorald does not gather statistics.")
    File statsOutputFile;
}
