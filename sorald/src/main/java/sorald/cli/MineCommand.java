package sorald.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;
import picocli.CommandLine;
import sorald.Constants;
import sorald.FileUtils;
import sorald.SoraldConfig;
import sorald.event.StatsMetadataKeys;
import sorald.event.collectors.MinerStatisticsCollector;
import sorald.event.models.ExecutionInfo;
import sorald.miner.MineSonarWarnings;
import sorald.rule.IRuleType;
import sorald.rule.Rule;
import sorald.rule.RuleProvider;
import sorald.sonar.SonarRule;
import sorald.sonar.SonarRuleType;
import sorald.util.MavenUtils;

/** CLI Command for Sorald's mining functionality. */
@Mojo(name = Constants.MINE_COMMAND_NAME)
@CommandLine.Command(
        name = Constants.MINE_COMMAND_NAME,
        mixinStandardHelpOptions = true,
        description = "Mine a project for Sonar warnings.")
class MineCommand extends BaseCommand {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    @CommandLine.Option(
            names = {Constants.ARG_SOURCE},
            description = "The path to the file or folder to be analyzed and possibly repaired.")
    File source;

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
            converter = IRuleTypeConverter.class,
            completionCandidates = RuleTypeCandidates.class,
            description =
                    "One or more types of rules to check for (use ',' to separate multiple types). Choices: ${COMPLETION-CANDIDATES}",
            split = ",")
    private List<IRuleType> ruleTypes = new ArrayList<>();

    @Parameter(property = "handledRules")
    @CommandLine.Option(
            names = {Constants.ARG_HANDLED_RULES},
            description =
                    "When this argument is used, Sorald only mines violations of the rules that can be fixed by Sorald.")
    private boolean handledRules;

    @CommandLine.Option(
            names = {Constants.ARG_RULE_KEYS},
            arity = "1..*",
            description =
                    "One or more rules to check for (use ',' to separate multiple types). Usage of this argument voids values of other rule filters - handled rules and rule types.",
            split = ",")
    List<String> ruleKeys;

    @CommandLine.Option(
            names = {Constants.ARG_RULE_PARAMETERS},
            description = {
                "Configuration for SonarJava rules.",
                "Format of JSON file: {%n"
                        + "    \"<RULE_KEY>\": {%n"
                        + "        \"<RULE_PROPERTY_NAME>\": \"<VALUE>\"%n"
                        + "    }%n"
                        + "}"
            })
    private File ruleParameters;

    @Override
    public Integer call() throws Exception {
        validateArgs();

        List<Rule> checks;
        // If rule keys are specified, we ignore other rule filters
        // ruleKeys is null for CLI, but an empty list for maven-plugin if not provided
        if (ruleKeys == null || ruleKeys.isEmpty()) {
            checks = RuleProvider.inferRules(ruleTypes, handledRules);
        } else {
            checks = ruleKeys.stream().map(SonarRule::new).collect(Collectors.toList());
        }

        var statsCollector = new MinerStatisticsCollector();

        var miner =
                new MineSonarWarnings(
                        statsOutputFile == null ? List.of() : List.of(statsCollector),
                        createConfig());

        if (statsOnGitRepos) {
            List<String> reposList = Files.readAllLines(this.reposList.toPath());
            miner.mineGitRepos(checks, minerOutputFile.getAbsolutePath(), reposList, tempDir);
        } else {
            miner.mineLocalProject(checks, source.toPath().normalize().toAbsolutePath().toString());
        }

        if (statsOutputFile != null) {
            List<String> originalArgs;
            // If the plugin is used, the original arguments are stored in the mojo descriptor
            if (mavenArgs != null) {
                originalArgs = mavenArgs;
            }
            // If the CLI is used, the original arguments are stored in the command line spec of
            // PicoCLI
            else {
                originalArgs = spec.commandLine().getParseResult().originalArgs();
            }
            Map<String, Object> additionalStatData =
                    Map.of(
                            StatsMetadataKeys.EXECUTION_INFO,
                            new ExecutionInfo(
                                    originalArgs,
                                    SoraldVersionProvider.getVersionFromManifests(
                                            SoraldVersionProvider.DEFAULT_RESOURCE_NAME),
                                    System.getProperty(Constants.JAVA_VERSION_SYSTEM_PROPERTY),
                                    target));

            FileUtils.writeJSON(statsOutputFile, statsCollector, additionalStatData);
        }

        return 0;
    }

    /** Perform validation on the parsed arguments. */
    private void validateArgs() {
        if (resolveClasspathFrom != null
                && !MavenUtils.isMavenProjectRoot(resolveClasspathFrom.toPath())) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    String.format(
                            "%s is only supported for Maven projects, but %s has no pom.xml",
                            Constants.ARG_RESOLVE_CLASSPATH_FROM, source));
        }
        if (ruleParameters != null && !ruleParameters.exists()) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(), String.format("%s is not a valid file", ruleParameters));
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        mavenArgs = getMavenArgs();
        try {
            call();
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    private static class IRuleTypeConverter implements CommandLine.ITypeConverter<IRuleType> {
        @Override
        public IRuleType convert(String value) {
            return SonarRuleType.valueOf(value.toUpperCase());
        }
    }

    private static class RuleTypeCandidates extends ArrayList<String> {
        private static final long serialVersionUID = 1L;

        RuleTypeCandidates() {
            super(
                    Arrays.stream(SonarRuleType.values())
                            .map(Enum::toString)
                            .collect(Collectors.toList()));
        }
    }

    private CLIConfigForStaticAnalyzer createConfig() throws IOException {
        SoraldConfig config = new SoraldConfig();
        config.setClasspath(resolveClasspath());
        config.setRuleParameters(parseRuleParameters());
        return config;
    }

    private List<String> resolveClasspath() {
        return resolveClasspathFrom != null
                ? MavenUtils.resolveClasspath(resolveClasspathFrom.toPath())
                : List.of();
    }

    private Map<Rule, Map<String, String>> parseRuleParameters() throws IOException {
        if (ruleParameters == null) {
            return new HashMap<>();
        }
        JSONObject ruleParametersAsJson = FileUtils.readJSON(ruleParameters.toPath());
        Map<Rule, Map<String, String>> result = new HashMap<>();
        for (String rule : ruleParametersAsJson.keySet()) {
            JSONObject parameters = (JSONObject) ruleParametersAsJson.get(rule);
            Map<String, String> options = new HashMap<>();
            for (String option : parameters.keySet()) {
                options.put(option, (String) parameters.get(option));
            }
            result.put(new SonarRule(rule), options);
        }
        return result;
    }
}
