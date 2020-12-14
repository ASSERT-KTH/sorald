package sorald.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import picocli.CommandLine;
import sorald.Constants;
import sorald.FileOutputStrategy;
import sorald.FileUtils;
import sorald.PrettyPrintingStrategy;
import sorald.Processors;
import sorald.Repair;
import sorald.RepairStrategy;
import sorald.SoraldConfig;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.StatsMetadataKeys;
import sorald.event.collectors.RepairStatisticsCollector;
import sorald.event.models.ExecutionInfo;
import sorald.event.models.repair.RuleRepairStatistics;
import sorald.sonar.RuleViolation;

/** The CLI command for the primary repair application. */
@CommandLine.Command(
        name = Constants.REPAIR_COMMAND_NAME,
        mixinStandardHelpOptions = true,
        description = "Repair Sonar rule violations in a targeted project.")
class RepairCommand extends BaseCommand {
    List<Integer> ruleKeys;
    List<RuleViolation> ruleViolations = List.of();

    @CommandLine.Option(
            names = {Constants.ARG_ORIGINAL_FILES_PATH},
            description = "The path to the file or folder to be analyzed and possibly repaired.",
            required = true)
    File originalFilesPath;

    @CommandLine.ArgGroup(multiplicity = "1")
    Rules rules;

    static class Rules {
        @CommandLine.Option(
                names = {Constants.ARG_RULE_KEYS},
                description =
                        "Choose one or more of the following rule keys "
                                + "(use ',' to separate multiple keys):\n"
                                + Processors.RULE_DESCRIPTIONS,
                required = true,
                split = ",")
        List<Integer> ruleKeys = List.of();

        @CommandLine.Option(
                names = Constants.ARG_RULE_VIOLATION_SPECIFIERS,
                description =
                        "One or more rule violation specifiers. Specifiers can be gathered "
                                + "with the '"
                                + Constants.MINE_COMMAND_NAME
                                + "' command using the "
                                + Constants.ARG_STATS_OUTPUT_FILE
                                + " option.",
                required = true,
                split = ",")
        List<String> ruleViolationSpecifiers = List.of();
    }

    @CommandLine.Option(
            names = {Constants.ARG_WORKSPACE},
            description =
                    "The path to a folder that will be used as workspace by Sorald, i.e. the path for the output.",
            defaultValue = Constants.SORALD_WORKSPACE)
    File soraldWorkspace;

    @CommandLine.Option(
            names = {Constants.ARG_GIT_REPO_PATH},
            description = "The path to a git repository directory.")
    File gitRepoPath;

    @CommandLine.Option(
            names = {Constants.ARG_PRETTY_PRINTING_STRATEGY},
            description =
                    "Mode for pretty printing the source code: 'NORMAL', which means that all source code will be printed and its formatting might change (such as indentation), and 'SNIPER', which means that only statements changed towards the repair of Sonar rule violations will be printed.")
    PrettyPrintingStrategy prettyPrintingStrategy = PrettyPrintingStrategy.SNIPER;

    @CommandLine.Option(
            names = Constants.ARG_FILE_OUTPUT_STRATEGY,
            description =
                    "Mode for outputting files: 'CHANGED_ONLY', which means that only changed files will be created in the workspace. 'ALL', which means that all files, including the unchanged ones, will be created in the workspace. 'IN_PLACE', which means that results are written directly to source files.")
    FileOutputStrategy fileOutputStrategy = FileOutputStrategy.CHANGED_ONLY;

    @CommandLine.Option(
            names = Constants.ARG_MAX_FIXES_PER_RULE,
            description = "Max number of fixes per rule.")
    int maxFixesPerRule = Integer.MAX_VALUE;

    @CommandLine.Option(
            names = Constants.ARG_REPAIR_STRATEGY,
            description =
                    "Type of repair strategy. DEFAULT - load everything without splitting up the folder in segments, SEGMENT - splitting the folder into smaller segments and repair one segment at a time (need to specify --maxFilesPerSegment if not default)")
    RepairStrategy repairStrategy = RepairStrategy.DEFAULT;

    @CommandLine.Option(
            names = Constants.ARG_MAX_FILES_PER_SEGMENT,
            description =
                    "Max number of files per loaded segment for segmented repair. It should be >= 3000 files per segment.")
    int maxFilesPerSegment = 6500;

    @Override
    public Integer call() throws IOException {
        postprocessArgs();
        validateArgs();
        SoraldConfig config = createConfig();

        var statsCollector = new RepairStatisticsCollector();
        var eventHandlers = List.of(statsCollector);
        EventHelper.fireEvent(EventType.EXEC_START, eventHandlers);

        var repair = new Repair(config, statsOutputFile == null ? List.of() : eventHandlers);
        repair.repair();

        EventHelper.fireEvent(EventType.EXEC_END, List.of(statsCollector));

        if (statsOutputFile != null) {
            mineWarningsAfter(repair, config.getRuleKeys());
            writeStatisticsOutput(
                    statsCollector,
                    FileUtils.getClosestDirectory(originalFilesPath)
                            .toPath()
                            .toAbsolutePath()
                            .normalize());
        }

        return 0;
    }

    /**
     * Mine warnings after completing repairs to trigger new mined events for the stats collection.
     */
    private void mineWarningsAfter(Repair repair, List<Integer> ruleKeys) {
        File projectPath = originalFilesPath.toPath().toAbsolutePath().normalize().toFile();
        ruleKeys.forEach(key -> repair.mineViolations(projectPath, key));
    }

    private void writeStatisticsOutput(RepairStatisticsCollector statsCollector, Path projectPath)
            throws IOException {
        var executionInfo =
                new ExecutionInfo(
                        spec.commandLine().getParseResult().originalArgs(),
                        SoraldVersionProvider.getVersionFromPropertiesResource(
                                SoraldVersionProvider.DEFAULT_RESOURCE_NAME),
                        System.getProperty(Constants.JAVA_VERSION_SYSTEM_PROPERTY),
                        target);

        List<RuleRepairStatistics> repairStats =
                RuleRepairStatistics.createRepairStatsList(statsCollector, projectPath);

        FileUtils.writeJSON(
                statsOutputFile,
                statsCollector,
                Map.of(
                        StatsMetadataKeys.EXECUTION_INFO,
                        executionInfo,
                        StatsMetadataKeys.REPAIRS,
                        repairStats));
    }

    private void validateArgs() {
        if (maxFilesPerSegment <= 0) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    Constants.ARG_MAX_FILES_PER_SEGMENT + " must be greater than 0");
        }

        validateRuleKeys();
    }

    /** Perform further processing of raw command line args. */
    private void postprocessArgs() {
        ruleViolations = parseRuleViolations(rules);
        ruleKeys = parseRuleKeys(rules, ruleViolations);
    }

    private List<RuleViolation> parseRuleViolations(Rules rules) {
        return rules.ruleViolationSpecifiers.stream()
                .map(this::parseRuleViolation)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<Integer> parseRuleKeys(Rules rules, List<RuleViolation> ruleViolations) {
        return ruleViolations.isEmpty()
                ? rules.ruleKeys
                : ruleViolations.stream()
                        .map(RuleViolation::getRuleKey)
                        .map(Integer::parseInt)
                        .collect(Collectors.toUnmodifiableList());
    }

    private void validateRuleKeys() {
        for (Integer ruleKey : ruleKeys) {
            if (Processors.getProcessor(ruleKey) == null) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        "Sorry, repair not available for rule "
                                + ruleKey
                                + ". See the available rules below.");
            }
        }
    }

    private RuleViolation parseRuleViolation(String violationSpecifier) {
        String[] parts = violationSpecifier.split(Constants.VIOLATION_SPECIFIER_SEP);
        String key = parts[0];
        String rawFilename = parts[1];
        String fileName =
                originalFilesPath.toPath().resolve(rawFilename).toAbsolutePath().toString();

        if (!new File(fileName).isFile()) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    String.format(
                            "Invalid violation ID '%s', no file '%s' in directory '%s'",
                            violationSpecifier, rawFilename, originalFilesPath));
        }

        int startLine = Integer.parseInt(parts[2]);
        int startCol = Integer.parseInt(parts[3]);
        int endLine = Integer.parseInt(parts[4]);
        int endCol = Integer.parseInt(parts[5]);
        return new SpecifiedViolation(key, fileName, startLine, startCol, endLine, endCol);
    }

    private SoraldConfig createConfig() {
        SoraldConfig config = new SoraldConfig();
        config.addRuleKeys(ruleKeys);
        config.setRuleViolations(ruleViolations);
        config.setOriginalFilesPath(originalFilesPath.getAbsolutePath());
        config.setWorkspace(soraldWorkspace.getAbsolutePath());
        if (gitRepoPath != null) {
            config.setGitRepoPath(gitRepoPath.getAbsolutePath());
        }
        config.setPrettyPrintingStrategy(prettyPrintingStrategy);
        config.setFileOutputStrategy(fileOutputStrategy);
        config.setMaxFixesPerRule(maxFixesPerRule);
        config.setMaxFilesPerSegment(maxFilesPerSegment);
        config.setRepairStrategy(repairStrategy);
        config.setStatsOutputFile(statsOutputFile);
        return config;
    }
}
