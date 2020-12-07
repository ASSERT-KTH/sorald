package sorald.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sorald.Constants;
import sorald.FileOutputStrategy;
import sorald.FileUtils;
import sorald.PrettyPrintingStrategy;
import sorald.Processors;
import sorald.Repair;
import sorald.RepairStrategy;
import sorald.SoraldConfig;
import sorald.event.StatisticsCollector;
import sorald.event.StatsMetadataKeys;
import sorald.sonar.RuleViolation;

/** The CLI command for the primary repair application. */
@CommandLine.Command(
        name = Constants.REPAIR_COMMAND_NAME,
        mixinStandardHelpOptions = true,
        description = "Repair Sonar rule violations in a targeted project.")
class RepairCommand implements Callable<Integer> {
    List<Integer> ruleKeys;
    List<RuleViolation> ruleViolations = List.of();

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

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
        List<Integer> ruleKeys;

        @CommandLine.Option(
                names = Constants.ARG_RULE_VIOLATIONS,
                description = "One or more specific rule violations",
                required = true,
                split = ",")
        List<String> ruleViolations = List.of();
    }

    private void parseRuleKeys(List<Integer> value) {
        for (Integer ruleKey : value) {
            if (Processors.getProcessor(ruleKey) == null) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        "Sorry, repair not available for rule "
                                + ruleKey
                                + ". See the available rules below.");
            }
        }
        ruleKeys = value;
    }

    private void parseRuleViolations(List<String> value) {
        List<RuleViolation> parsedViolations = new ArrayList<>();
        List<Integer> keys = new ArrayList<>();
        for (String specifier : value) {
            String[] parts = specifier.split(":");
            String key = parts[0];
            keys.add(Integer.parseInt(key));
            String fileName =
                    originalFilesPath.toPath().resolve(parts[1]).toAbsolutePath().toString();
            int startLine = Integer.parseInt(parts[2]);
            int startCol = Integer.parseInt(parts[3]);
            int endLine = Integer.parseInt(parts[4]);
            int endCol = Integer.parseInt(parts[5]);
            parsedViolations.add(
                    new SpecifiedRuleViolation(
                            key, fileName, startLine, endLine, startCol, endCol));
        }
        ruleViolations = parsedViolations;
        ruleKeys = keys;
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

    @CommandLine.Option(
            names = Constants.ARG_STATS_OUTPUT_FILE,
            description =
                    "Path to a file to store execution statistics in (in JSON format). If left unspecified, Sorald does not gather statistics.")
    File statsOutputFile;

    @Override
    public Integer call() throws IOException {
        validateArgs();

        if (rules.ruleKeys != null) {
            parseRuleKeys(rules.ruleKeys);
        } else {
            parseRuleViolations(rules.ruleViolations);
        }

        SoraldConfig config = createConfig();

        var statsCollector = new StatisticsCollector();
        new Repair(config, statsOutputFile == null ? List.of() : List.of(statsCollector)).repair();

        if (statsOutputFile != null) {
            FileUtils.writeJSON(
                    statsOutputFile,
                    statsCollector,
                    Map.of(
                            StatsMetadataKeys.ORIGINAL_ARGS,
                            spec.commandLine().getParseResult().originalArgs()));
        }

        return 0;
    }

    private void validateArgs() {
        if (maxFilesPerSegment <= 0) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    Constants.ARG_MAX_FILES_PER_SEGMENT + " must be greater than 0");
        }

        if (statsOutputFile != null && repairStrategy == RepairStrategy.SEGMENT) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    RepairStrategy.SEGMENT.name()
                            + " repair does not currently support statistics collection");
        }
    }

    private SoraldConfig createConfig() {
        SoraldConfig config = new SoraldConfig();
        config.addRuleKeys(ruleKeys);
        config.addRuleViolations(ruleViolations);
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
