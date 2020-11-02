package sorald.miner;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.sonar.Checks;
import sorald.sonar.RuleVerifier;
import sorald.sonar.RuleViolation;

public class MineSonarWarnings {

    public static JSAP defineArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption opt = new FlaggedOption(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setLongFlag(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the file or folder to be analyzed.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_STATS_ON_GIT_REPOS);
        opt.setLongFlag(Constants.ARG_STATS_ON_GIT_REPOS);
        opt.setStringParser(BooleanStringParser.getParser());
        opt.setRequired(false);
        opt.setHelp("If the stats should be computed on git repos.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_STATS_OUTPUT_FILE);
        opt.setLongFlag(Constants.ARG_STATS_OUTPUT_FILE);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the output file.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_GIT_REPOS_LIST);
        opt.setLongFlag(Constants.ARG_GIT_REPOS_LIST);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the repos list.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_TEMP_DIR);
        opt.setLongFlag(Constants.ARG_TEMP_DIR);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the temp directory.");
        jsap.registerParameter(opt);

        // TODO don't reuse the opt variable, declare one variable per option
        opt = new FlaggedOption(Constants.ARG_RULE_TYPES);
        opt.setLongFlag(Constants.ARG_RULE_TYPES);
        opt.setList(true);
        opt.setListSeparator(',');
        opt.setRequired(false);
        String ruleTypes = String.join(", ", Constants.SONAR_RULE_TYPES);
        opt.setHelp("One or more types of rules to check for. Choices: " + ruleTypes);
        jsap.registerParameter(opt);

        Switch sw = new Switch("help");
        sw.setShortFlag('h');
        sw.setLongFlag("help");
        sw.setDefault("false");
        jsap.registerParameter(sw);

        return jsap;
    }

    public static void checkArguments(JSAP jsap, JSAPResult arguments) {
        if (!arguments.success()) {
            for (java.util.Iterator<?> errors = arguments.getErrorMessageIterator();
                    errors.hasNext(); ) {
                System.err.println("Error: " + errors.next());
            }
            printUsage(jsap);
        }

        if (arguments.getBoolean("help")) {
            printUsage(jsap);
        }
    }

    public static void printUsage(JSAP jsap) {
        System.err.println("Arguments: ");
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(-1);
    }

    public static void main(String[] args) throws JSAPException, IOException, GitAPIException {
        JSAP jsap = defineArgs();
        JSAPResult arguments = jsap.parse(args);
        checkArguments(jsap, arguments);
        List<? extends JavaFileScanner> checks =
                inferCheckInstances(arguments.getStringArray(Constants.ARG_RULE_TYPES));

        if (arguments.contains(Constants.ARG_STATS_ON_GIT_REPOS)) {
            // stats on a list of git repos
            String outputPath =
                    arguments.getFile(Constants.ARG_STATS_OUTPUT_FILE).getAbsolutePath();
            String reposListFilePath =
                    arguments.getFile(Constants.ARG_GIT_REPOS_LIST).getAbsolutePath();
            File repoDir = new File(arguments.getFile(Constants.ARG_TEMP_DIR).getAbsolutePath());

            List<String> reposList = getReposList(reposListFilePath);

            for (String repo : reposList) {
                String repoName = repo.substring(repo.lastIndexOf('/') + 1, repo.lastIndexOf("."));

                FileUtils.cleanDirectory(repoDir);

                boolean isCloned = false;

                try {
                    Git git = Git.cloneRepository().setURI(repo).setDirectory(repoDir).call();
                    git.close();
                    isCloned = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, Integer> warnings = extractWarnings(repoDir.getAbsolutePath(), checks);

                PrintWriter pw = new PrintWriter(new FileWriter(outputPath, true));

                if (isCloned) {
                    pw.println("RepoName: " + repoName);

                    warnings.entrySet().stream()
                            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                            .forEach(pw::println);
                } else {
                    pw.println("RepoName: " + repoName + " not_cloned");
                }

                pw.flush();
                pw.close();
            }

        } else { // default mode

            String projectPath =
                    arguments.getFile(Constants.ARG_ORIGINAL_FILES_PATH).getAbsolutePath();

            Map<String, Integer> warnings = extractWarnings(projectPath, checks);

            warnings.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .forEach(System.out::println);
        }
    }

    private static List<String> getReposList(String reposListFilePath)
            throws FileNotFoundException {
        List<String> res = new ArrayList<>();

        Scanner sc = new Scanner(new File(reposListFilePath));

        while (sc.hasNextLine()) {
            res.add(sc.nextLine());
        }

        sc.close();

        return res;
    }

    /**
     * @param projectPath The root path to a Java project
     * @param checks Checks to run on the Java files in the project
     * @return A mapping (checkClassName -> numViolations)
     */
    static Map<String, Integer> extractWarnings(
            String projectPath, List<? extends JavaFileScanner> checks) {
        List<String> filesToScan = new ArrayList<>();
        File file = new File(projectPath);
        if (file.isFile()) {
            filesToScan.add(file.getAbsolutePath());
        } else {
            try (Stream<Path> walk = Files.walk(Paths.get(file.getAbsolutePath()))) {
                filesToScan =
                        walk.map(x -> x.toFile().getAbsolutePath())
                                .filter(f -> f.endsWith(Constants.JAVA_EXT))
                                .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Map<String, Integer> warnings = new HashMap<>();
        checks.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .forEach(checkName -> warnings.put(checkName, 0));

        Consumer<String> incrementWarningCount =
                (checkName) -> warnings.put(checkName, warnings.get(checkName) + 1);
        RuleVerifier.analyze(filesToScan, file, checks).stream()
                .map(RuleViolation::getCheckName)
                .forEach(incrementWarningCount);
        return warnings;
    }

    /**
     * Infer which check instances to use based on rule types specified (or left unspecified) on the
     * command line.
     */
    private static List<? extends JavaFileScanner> inferCheckInstances(String[] ruleTypes) {
        List<Checks.CheckType> checkTypes =
                Arrays.stream(ruleTypes)
                        .map(Checks.CheckType::fromLabel)
                        .collect(Collectors.toList());
        return checkTypes.isEmpty() ? getAllCheckInstances() : getCheckInstancesByTypes(checkTypes);
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
