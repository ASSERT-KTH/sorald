package sorald.cli;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.sonar.plugins.java.api.JavaFileScanner;
import picocli.CommandLine;
import sorald.Constants;
import sorald.FileUtils;
import sorald.event.StatsMetadataKeys;
import sorald.event.collectors.MinerStatisticsCollector;
import sorald.event.models.ExecutionInfo;
import sorald.miner.MineSonarWarnings;
import sorald.sonar.Checks;

/** Class containing the CLI for Sorald. */
public class Cli {
    private static String javaVersion;

    static {
        javaVersion = System.getProperty(Constants.JAVA_VERSION_SYSTEM_PROPERTY);
    }

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
            synopsisSubcommandLabel = "<COMMAND>")
    static class SoraldCLI implements Callable<Integer> {

        @Override
        public Integer call() {
            new CommandLine(this).usage(System.out);
            return -1;
        }
    }

    @CommandLine.Command(
            name = Constants.MINE_COMMAND_NAME,
            mixinStandardHelpOptions = true,
            description = "Mine a project for Sonar warnings.")
    private static class MineCommand implements Callable<Integer> {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
                names = {Constants.ARG_ORIGINAL_FILES_PATH},
                description =
                        "The path to the file or folder to be analyzed and possibly repaired.")
        File originalFilesPath;

        @CommandLine.Option(
                names = Constants.ARG_STATS_ON_GIT_REPOS,
                description = "If the stats should be computed on git repos.")
        boolean statsOnGitRepos;

        @CommandLine.Option(
                names = Constants.ARG_STATS_OUTPUT_FILE,
                description = "The path to the stats output file.")
        File statsOutputFile;

        @CommandLine.Option(
                names = Constants.ARG_MINER_OUTPUT_FILE,
                description = "The path to the output file.")
        File minerOutputFile;

        @CommandLine.Option(
                names = Constants.ARG_GIT_REPOS_LIST,
                description = "The path to the repos list.")
        File reposList;

        @CommandLine.Option(
                names = Constants.ARG_TEMP_DIR,
                description = "The path to the temp directory.")
        File tempDir;

        @CommandLine.Option(
                names = {Constants.ARG_RULE_TYPES},
                description =
                        "One or more types of rules to check for (use ',' to separate multiple types). Choices: ${COMPLETION-CANDIDATES}",
                split = ",")
        private List<Checks.CheckType> ruleTypes = new ArrayList<>();

        @CommandLine.Option(
                names = Constants.ARG_TARGET,
                description =
                        "The target of this execution (ex. sorald/92d377). This will be included in the json report.")
        String target;

        @Override
        public Integer call() throws Exception {
            List<? extends JavaFileScanner> checks = inferCheckInstances(ruleTypes);

            var statsCollector = new MinerStatisticsCollector();

            if (statsOnGitRepos) {
                List<String> reposList = Files.readAllLines(this.reposList.toPath());

                new MineSonarWarnings(statsOutputFile == null ? List.of() : List.of(statsCollector))
                        .mineGitRepos(
                                checks, minerOutputFile.getAbsolutePath(), reposList, tempDir);
            } else {
                new MineSonarWarnings(statsOutputFile == null ? List.of() : List.of(statsCollector))
                        .mineLocalProject(checks, originalFilesPath.getAbsolutePath());
            }

            if (statsOutputFile != null) {
                Map<String, Object> additionalStatData =
                        Map.of(
                                StatsMetadataKeys.EXECUTION_INFO,
                                new ExecutionInfo(
                                        spec.commandLine().getParseResult().originalArgs(),
                                        Constants.SORALD_VERSION,
                                        javaVersion,
                                        target));

                FileUtils.writeJSON(statsOutputFile, statsCollector, additionalStatData);
            }

            return 0;
        }

        /**
         * Infer which check instances to use based on rule types specified (or left unspecified) on
         * the command line.
         */
        private static List<? extends JavaFileScanner> inferCheckInstances(
                List<Checks.CheckType> ruleTypes) {
            return ruleTypes.isEmpty()
                    ? getAllCheckInstances()
                    : getCheckInstancesByTypes(ruleTypes);
        }

        private static List<? extends JavaFileScanner> getCheckInstancesByTypes(
                List<Checks.CheckType> checkTypes) {
            return checkTypes.stream()
                    .map(Checks::getChecksByType)
                    .flatMap(Collection::stream)
                    .map(Checks::instantiateCheck)
                    .collect(Collectors.toList());
        }

        private static List<? extends JavaFileScanner> getAllCheckInstances() {
            return Checks.getAllChecks().stream()
                    .map(Checks::instantiateCheck)
                    .collect(Collectors.toList());
        }
    }
}
