package sorald.cli;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import org.sonar.plugins.java.api.JavaFileScanner;
import picocli.CommandLine;
import sorald.Constants;
import sorald.FileUtils;
import sorald.Processors;
import sorald.event.StatsMetadataKeys;
import sorald.event.collectors.MinerStatisticsCollector;
import sorald.event.models.ExecutionInfo;
import sorald.miner.MineSonarWarnings;
import sorald.sonar.Checks;

/** CLI Command for Sorald's mining functionality. */
@CommandLine.Command(
        name = Constants.MINE_COMMAND_NAME,
        mixinStandardHelpOptions = true,
        description = "Mine a project for Sonar warnings.")
class MineCommand extends BaseCommand {

    @CommandLine.Option(
            names = {Constants.ARG_ORIGINAL_FILES_PATH},
            description = "The path to the file or folder to be analyzed and possibly repaired.")
    File originalFilesPath;

    @CommandLine.Option(
            names = Constants.ARG_STATS_ON_GIT_REPOS,
            description = "If the stats should be computed on git repos.")
    boolean statsOnGitRepos;

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
            names = {Constants.ARG_HANDLED_RULES},
            description =
                    "When this argument is used, Sorald only mines violations of the rules that can be fixed by Sorald.")
    private boolean handledRules;

    @Override
    public Integer call() throws Exception {
        List<? extends JavaFileScanner> checks = inferCheckInstances(ruleTypes, handledRules);

        var statsCollector = new MinerStatisticsCollector();

        if (statsOnGitRepos) {
            List<String> reposList = Files.readAllLines(this.reposList.toPath());

            new MineSonarWarnings(statsOutputFile == null ? List.of() : List.of(statsCollector))
                    .mineGitRepos(checks, minerOutputFile.getAbsolutePath(), reposList, tempDir);
        } else {
            new MineSonarWarnings(statsOutputFile == null ? List.of() : List.of(statsCollector))
                    .mineLocalProject(
                            checks,
                            originalFilesPath.toPath().normalize().toAbsolutePath().toString());
        }

        if (statsOutputFile != null) {
            Map<String, Object> additionalStatData =
                    Map.of(
                            StatsMetadataKeys.EXECUTION_INFO,
                            new ExecutionInfo(
                                    spec.commandLine().getParseResult().originalArgs(),
                                    SoraldVersionProvider.getVersionFromPropertiesResource(
                                            SoraldVersionProvider.DEFAULT_RESOURCE_NAME),
                                    System.getProperty(Constants.JAVA_VERSION_SYSTEM_PROPERTY),
                                    target));

            FileUtils.writeJSON(statsOutputFile, statsCollector, additionalStatData);
        }

        return 0;
    }

    /**
     * Infer which check instances to use based on rule types specified (or left unspecified) on the
     * command line.
     */
    private static List<? extends JavaFileScanner> inferCheckInstances(
            List<Checks.CheckType> ruleTypes, boolean handledRules) {
        List<? extends JavaFileScanner> checks =
                ruleTypes.isEmpty() ? getAllCheckInstances() : getCheckInstancesByTypes(ruleTypes);

        checks =
                !handledRules
                        ? checks
                        : checks.stream()
                                .filter(
                                        sc -> {
                                            int key =
                                                    Integer.parseInt(
                                                            Checks.getRuleKey(sc.getClass()));
                                            return Processors.getProcessor(key) != null;
                                        })
                                .collect(Collectors.toList());

        return checks;
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
