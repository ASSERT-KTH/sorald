package sorald.cli;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
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
import sorald.segment.FirstFitSegmentationAlgorithm;
import sorald.segment.Node;
import sorald.segment.SoraldTreeBuilderAlgorithm;

public class Cli {

    public static CommandLine createCli() {
        return new CommandLine(new RepairCommand());
    }

    @CommandLine.Command(
            mixinStandardHelpOptions = true,
            description = "Sorald automatic repair tool.")
    private static class RepairCommand implements Callable<Integer> {
        private List<Integer> ruleKeys;

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

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
                names = {Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH},
                description =
                        "The path to the file or folder to be analyzed and possibly repaired.",
                required = true)
        File originalFilesPath;

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
        public Integer call() throws Exception {
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
}
