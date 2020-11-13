package sorald.cli;

import picocli.CommandLine;
import sorald.Constants;
import sorald.Processors;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class Cli {

    public static void main(String[] args) {
        new CommandLine(new TestCmd())
                .execute("--ruleKeys", "2755", "--originalFilesPath", "/path/to/file");
    }

    @CommandLine.Command(name = "sorald", mixinStandardHelpOptions = true, description = "hello")
    static class TestCmd implements Callable<Integer> {
        private int[] ruleKeys;

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
                names = {Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS},
                description =
                        "Choose one or more of the following rule keys "
                                + "(use ',' to separate multiple keys):"
                                + Processors.RULE_DESCRIPTIONS,
                required = true,
                split = ",")
        private void setRuleKeys(int[] value) {
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

        @CommandLine.Option(names = {Constants.ARG_SYMBOL + Constants.ARG_GIT_REPO_PATH},
                description = "The path to a git repository directory."
        )
        File gitRepoPath;



        @Override
        public Integer call() throws Exception {
            System.out.println(Arrays.toString(ruleKeys));
            System.out.println(originalFilesPath);
            System.out.println(soraldWorkspace.toPath());
            return 1;
        }
    }
}
