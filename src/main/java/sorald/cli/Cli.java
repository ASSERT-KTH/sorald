package sorald.cli;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.JavaFileScanner;
import picocli.CommandLine;
import sorald.Constants;
import sorald.DefaultRepair;
import sorald.FileOutputStrategy;
import sorald.PrettyPrintingStrategy;
import sorald.Processors;
import sorald.RepairStrategy;
import sorald.SegmentRepair;
import sorald.SoraldAbstractRepair;
import sorald.SoraldConfig;
import sorald.miner.MineSonarWarnings;
import sorald.segment.FirstFitSegmentationAlgorithm;
import sorald.segment.Node;
import sorald.segment.SoraldTreeBuilderAlgorithm;
import sorald.sonar.Checks;

/** Class containing the CLI for Sorald. */
public class Cli {

    /** @return Sorald's command line interface. */
    public static CommandLine createCli() {
        return new CommandLine(new SoraldCLI());
    }

    @CommandLine.Command(
            mixinStandardHelpOptions = true,
            subcommands = {RepairCommand.class, MineCommand.class},
            description =
                    "The Sorald command line application for automatic repair of Sonar rule violations.",
            synopsisSubcommandLabel = "<COMMAND>")
    static class SoraldCLI implements Callable<Integer> {

        @Override
        public Integer call() {
            new CommandLine(this).usage(System.out);
            return 1;
        }
    }

    /**
     * Abstract command base class with the options that are in common for both the miner and repair
     * tool
     */
    private abstract static class CommandBase {
        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH},
                description =
                        "The path to the file or folder to be analyzed and possibly repaired.",
                required = true)
        File originalFilesPath;
    }

    /** The CLI command for the primary repair application. */
    @CommandLine.Command(
            name = Constants.REPAIR_COMMAND_NAME,
            mixinStandardHelpOptions = true,
            description = "Repair Sonar rule violations in a targeted project.")
    private static class RepairCommand extends CommandBase implements Callable<Integer> {
        private List<Integer> ruleKeys;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS},
                description =
                        "Choose one or more of the following rule keys "
                                + "(use ',' to separate multiple keys):\n"
                                + Processors.RULE_DESCRIPTIONS,
                required = true,
                split = ",")
        private void setRuleKeys(List<Integer> value) {
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

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE},
                description =
                        "The path to a folder that will be used as workspace by Sorald, i.e. the path for the output.",
                defaultValue = Constants.SORALD_WORKSPACE)
        File soraldWorkspace;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_GIT_REPO_PATH},
                description = "The path to a git repository directory.")
        File gitRepoPath;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY},
                description =
                        "Mode for pretty printing the source code: 'NORMAL', which means that all source code will be printed and its formatting might change (such as indentation), and 'SNIPER', which means that only statements changed towards the repair of Sonar rule violations will be printed.")
        PrettyPrintingStrategy prettyPrintingStrategy = PrettyPrintingStrategy.SNIPER;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_FILE_OUTPUT_STRATEGY,
                description =
                        "Mode for outputting files: 'CHANGED_ONLY', which means that only changed files will be created in the workspace, and 'ALL', which means that all files, including the unchanged ones, will be created in the workspace.")
        FileOutputStrategy fileOutputStrategy = FileOutputStrategy.CHANGED_ONLY;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_MAX_FIXES_PER_RULE,
                description = "Max number of fixes per rule.")
        int maxFixesPerRule = Integer.MAX_VALUE;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY,
                description =
                        "Type of repair strategy. DEFAULT - load everything without splitting up the folder in segments, SEGMENT - splitting the folder into smaller segments and repair one segment at a time (need to specify --maxFilesPerSegment if not default)")
        RepairStrategy repairStrategy = RepairStrategy.DEFAULT;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_MAX_FILES_PER_SEGMENT,
                description =
                        "Max number of files per loaded segment for segmented repair. It should be >= 3000 files per segment.")
        int maxFilesPerSegment = 6500;

        @Override
        public Integer call() {
            validateArgs();
            SoraldConfig config = createConfig();
            getRepairProcess(config).repair();
            return 0;
        }

        private void validateArgs() {
            if (maxFilesPerSegment <= 0) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        Constants.ARG_SYMBOL
                                + Constants.ARG_MAX_FILES_PER_SEGMENT
                                + " must be greater than 0");
            }
        }

        private SoraldConfig createConfig() {
            SoraldConfig config = new SoraldConfig();
            config.addRuleKeys(ruleKeys);
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
            return config;
        }

        private static SoraldAbstractRepair getRepairProcess(SoraldConfig config) {
            SoraldAbstractRepair repair;
            if (config.getRepairStrategy() == RepairStrategy.SEGMENT) {
                System.out.println("[Repair Mode] : SEGMENT");
                Node rootNode = SoraldTreeBuilderAlgorithm.buildTree(config.getOriginalFilesPath());
                LinkedList<LinkedList<Node>> segments =
                        FirstFitSegmentationAlgorithm.segment(
                                rootNode, config.getMaxFilesPerSegment());
                config.setSegments(segments);
                repair = new SegmentRepair(config);
            } else {
                assert config.getRepairStrategy() == RepairStrategy.DEFAULT;
                System.out.println("[Repair Mode] : DEFAULT");
                repair = new DefaultRepair(config);
            }
            return repair;
        }
    }

    @CommandLine.Command(
            name = Constants.MINE_COMMAND_NAME,
            mixinStandardHelpOptions = true,
            description = "Mine a project for Sonar warnings.")
    private static class MineCommand extends CommandBase implements Callable<Integer> {

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_STATS_ON_GIT_REPOS,
                description = "If the stats should be computed on git repos.")
        boolean statsOnGitRepos;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
                description = "The path to the output file.")
        File statsOutputFile;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_GIT_REPOS_LIST,
                description = "The path to the repos list.")
        File reposList;

        @CommandLine.Option(
                names = Constants.ARG_SYMBOL + Constants.ARG_TEMP_DIR,
                description = "The path to the temp directory.")
        File tempDir;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_RULE_TYPES},
                description =
                        "One or more types of rules to check for (use ',' to separate multiple types). Choices: ${COMPLETION-CANDIDATES}",
                split = ",")
        private List<Checks.CheckType> ruleTypes = new ArrayList<>();

        @Override
        public Integer call() throws Exception {
            List<? extends JavaFileScanner> checks = inferCheckInstances(ruleTypes);
            if (statsOnGitRepos) {
                List<String> reposList = Files.readAllLines(this.reposList.toPath());
                MineSonarWarnings.mineGitRepos(
                        checks, statsOutputFile.getAbsolutePath(), reposList, tempDir);
            } else {
                MineSonarWarnings.mineLocalProject(checks, originalFilesPath.getAbsolutePath());
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
