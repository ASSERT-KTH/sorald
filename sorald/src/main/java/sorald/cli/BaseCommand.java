package sorald.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugins.annotations.Parameter;
import picocli.CommandLine;
import sorald.Constants;

/** Base command containing the options in common for all Sorald subcommands. */
@CommandLine.Command()
abstract class BaseCommand extends AbstractMojo implements Callable<Integer> {
    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    List<String> mavenArgs;

    @CommandLine.Option(
            names = Constants.ARG_TARGET,
            description =
                    "The target of this execution (ex. sorald/92d377). This will be included in the json report.")
    String target;

    @Parameter(property = "statsOutputFile")
    @CommandLine.Option(
            names = Constants.ARG_STATS_OUTPUT_FILE,
            description =
                    "Path to a file to store execution statistics in (in JSON format). If left unspecified, Sorald does not gather statistics.")
    File statsOutputFile;

    @CommandLine.Option(
            names = Constants.ARG_RESOLVE_CLASSPATH_FROM,
            description =
                    "Path to the root of a project to resolve the classpath from. Currently only works for Maven projects.")
    File resolveClasspathFrom;

    /**
     * This method is used to capture the arguments passed to maven-plugin to display them in the
     * report.
     *
     * @param mojoContext the descriptor of the mojo {@link MojoDescriptor}
     * @return the list of arguments passed to the maven-plugin
     */
    protected List<String> getMavenArgs(MojoDescriptor mojoContext) {
        List<String> args = new ArrayList<>();
        args.add(mojoContext.getGoal());
        for (org.apache.maven.plugin.descriptor.Parameter parameter : mojoContext.getParameters()) {
            String parameterName = String.format("-D%s", parameter.getName());
            String parameterValue = System.getProperty(parameter.getName());
            args.add(parameterName);
            args.add(parameterValue);
        }
        return args;
    }
}
