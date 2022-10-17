package sorald.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
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

    static class FileConverterForMojo extends AbstractBasicConverter {
        @Override
        protected Object fromString(String userInput) {
            return new File(userInput);
        }

        @Override
        public boolean canConvert(Class<?> aClass) {
            return aClass.equals(File.class);
        }
    }

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

/**
 * Looks for children of {@link AbstractBasicConverter} and registers them. These converters ensure
 * type of value of {@link Parameter} is correctly converted to the type of the field as the parsed
 * type is always {@link String}.
 */
@Component(role = ComponentConfigurator.class, hint = "basic")
class MojoParametersCheckerAndConverter extends BasicComponentConfigurator
        implements Initializable {

    @Override
    public void initialize() {
        converterLookup.registerConverter(new BaseCommand.FileConverterForMojo());
        converterLookup.registerConverter(new RepairCommand.RulesConverterForMojo());
    }
}
