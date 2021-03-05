package sorald.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import sorald.event.SoraldEventHandler;
import sorald.event.StatsMetadataKeys;
import sorald.event.collectors.RepairStatisticsCollector;
import sorald.event.models.ExecutionInfo;
import sorald.event.models.miner.MinedViolationEvent;
import sorald.event.models.repair.RuleRepairStatistics;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;

/** The CLI command for the primary repair application. */
@CommandLine.Command(
        name = Constants.REPAIR_COMMAND_NAME,
        mixinStandardHelpOptions = true,
        description = "Repair Sonar rule violations in a targeted project.")
class RepairCommand extends BaseCommand {
    String ruleKey;
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
                names = {Constants.ARG_RULE_KEY},
                description =
                        "Choose one of the following rule keys:\n" + Processors.RULE_DESCRIPTIONS,
                required = true)
        String ruleKey = null;

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
                    "Type of repair strategy. DEFAULT - load everything without splitting up the folder in segments, "
                            + "MAVEN - use Maven to locate production source code and the classpath (test source code is ignored), "
                            + "SEGMENT - splitting the folder into smaller segments and repair one segment at a time (need to specify --maxFilesPerSegment if not default)")
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
        List<SoraldEventHandler> eventHandlers =
                statsOutputFile == null ? List.of() : List.of(statsCollector);
        EventHelper.fireEvent(EventType.EXEC_START, eventHandlers);

        Set<RuleViolation> ruleViolations = resolveRuleViolations(eventHandlers);
        var repair = new Repair(config, eventHandlers);
        repair.repair(ruleViolations);

        EventHelper.fireEvent(EventType.EXEC_END, List.of(statsCollector));

        if (statsOutputFile != null) {
            // mine violations to trigger stats collection
            mineViolations(originalFilesPath, ruleKey, eventHandlers);
            writeStatisticsOutput(
                    statsCollector,
                    FileUtils.getClosestDirectory(originalFilesPath)
                            .toPath()
                            .toAbsolutePath()
                            .normalize());
        }

        return 0;
    }

    private Set<RuleViolation> resolveRuleViolations(List<SoraldEventHandler> eventHandlers) {
        Set<RuleViolation> violations = null;
        if (!eventHandlers.isEmpty() || ruleViolations.isEmpty()) {
            // if there are event handlers, we must mine violations regardless of them being
            // specified in the config or not in order to trigger the mined violation events
            violations = mineViolations(originalFilesPath, ruleKey, eventHandlers);
        }
        if (!ruleViolations.isEmpty()) {
            violations = new HashSet<>(ruleViolations);
        }

        return violations;
    }

    /**
     * Mine violations from the target directory and the given rule key.
     *
     * @param target A target directory.
     * @param ruleKey A rule key.
     * @param eventHandlers Event handlers to use for events.
     * @return All found warnings.
     */
    private static Set<RuleViolation> mineViolations(
            File target, String ruleKey, List<SoraldEventHandler> eventHandlers) {
        Path projectPath = target.toPath().toAbsolutePath().normalize();
        Set<RuleViolation> violations =
                ProjectScanner.scanProject(
                        target,
                        FileUtils.getClosestDirectory(target),
                        Checks.getCheckInstance(ruleKey));
        violations.forEach(
                warn ->
                        EventHelper.fireEvent(
                                new MinedViolationEvent(warn, projectPath), eventHandlers));
        return violations;
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

        validateRuleKey();
    }

    /** Perform further processing of raw command line args. */
    private void postprocessArgs() {
        ruleViolations = parseRuleViolations(rules);
        ruleKey = parseRuleKey(rules, ruleViolations);
    }

    private List<RuleViolation> parseRuleViolations(Rules rules) {
        return rules.ruleViolationSpecifiers.stream()
                .map(this::parseRuleViolation)
                .collect(Collectors.toUnmodifiableList());
    }

    private String parseRuleKey(Rules rules, List<RuleViolation> ruleViolations) {
        return ruleViolations.isEmpty()
                ? rules.ruleKey
                : ruleViolations.stream().map(RuleViolation::getRuleKey).findFirst().get();
    }

    private void validateRuleKey() {
        if (Processors.getProcessor(Integer.parseInt(ruleKey)) == null) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "Sorry, repair not available for rule "
                            + ruleKey
                            + ". See the available rules below.");
        }
    }

    private RuleViolation parseRuleViolation(String violationSpecifier) {
        String[] parts = violationSpecifier.split(Constants.VIOLATION_SPECIFIER_SEP);
        String key = parts[0];
        String rawFilename = parts[1];
        Path absPath = originalFilesPath.toPath().resolve(rawFilename).toAbsolutePath().normalize();

        if (!absPath.toFile().isFile()) {
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
        return new SpecifiedViolation(key, absPath, startLine, startCol, endLine, endCol);
    }

    private SoraldConfig createConfig() {
        SoraldConfig config = new SoraldConfig();
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
