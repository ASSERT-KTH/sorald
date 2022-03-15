package sorald.sonar;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.AbstractSonarLintEngine;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.analysis.api.ActiveRule;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisEngineConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.analysis.command.AnalyzeCommand;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.exceptions.SonarLintWrappedException;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository.Configuration;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.FileUtils;
import sorald.Processors;
import sorald.cli.CommandConfiguration;
import sorald.rule.Rule;
import sorald.util.ConfigLoader;

public final class SonarLintEngine extends AbstractSonarLintEngine {

    // The order of these initialisations is important as each field is dependent upon the previous
    // one.
    private static final String SONAR_JAVA_PLUGIN_JAR_NAME = "sonar-java-plugin.jar";
    private static final Path sonarJavaPlugin = getOrDownloadSonarJavaPlugin().getPath();
    private static final StandaloneGlobalConfiguration globalConfig = buildGlobalConfig();
    private static final PluginInstancesRepositoryWhichCannotBeClosed pluginInstancesRepository =
            createPluginInstancesRepository();
    private static final Map<String, SonarLintRuleDefinition> allRulesDefinitionsByKey =
            computeAllRulesDefinitionsByKey();
    private static final AnalysisEngineConfiguration analysisGlobalConfig =
            buildAnalysisEngineConfiguration();

    // The only instance of this singleton class
    private static SonarLintEngine theOnlyInstance;

    // We need to reinitialise it before starting analysis of any source files on any rules.
    private AnalysisEngine analysisEngine;

    private SonarLintEngine() {
        super(null);
        setLogging(null);

        this.analysisEngine =
                new AnalysisEngine(analysisGlobalConfig, pluginInstancesRepository, null);
    }

    private static SonarJavaJarHolder getOrDownloadSonarJavaPlugin() {
        File cacheDirectory = FileUtils.getCacheDir();
        String sonarJavaPluginFileName =
                cacheDirectory + System.getProperty("file.separator") + SONAR_JAVA_PLUGIN_JAR_NAME;
        File sonarJavaPlugin = new File(sonarJavaPluginFileName);
        if (sonarJavaPlugin.exists()) {
            return new SonarJavaJarHolder(sonarJavaPlugin.toPath(), false);
        }

        try {
            InputStream inputStream = new URL(ConfigLoader.getSonarJavaPluginUrl()).openStream();
            Files.copy(
                    inputStream,
                    Paths.get(sonarJavaPluginFileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return new SonarJavaJarHolder(new File(sonarJavaPluginFileName).toPath(), true);
        } catch (IOException ignore) {
            throw new RuntimeException("Could not download Sonar Java plugin"); // NOSONAR:S112
        }
    }

    /** Store the path to SonarJava plugin and if it is downloaded or fetched from cache. */
    public static class SonarJavaJarHolder {
        private final Path path;
        private final boolean downloaded;

        SonarJavaJarHolder(Path path, boolean downloaded) {
            this.path = path;
            this.downloaded = downloaded;
        }

        public Path getPath() {
            return path;
        }

        public boolean isDownloaded() {
            return downloaded;
        }
    }

    private static StandaloneGlobalConfiguration buildGlobalConfig() {
        return StandaloneGlobalConfiguration.builder()
                .addPlugin(sonarJavaPlugin)
                .addEnabledLanguage(Language.JAVA)
                .build();
    }

    private static PluginInstancesRepositoryWhichCannotBeClosed createPluginInstancesRepository() {
        var config =
                new Configuration(
                        globalConfig.getPluginPaths(),
                        globalConfig.getEnabledLanguages(),
                        Optional.ofNullable(globalConfig.getNodeJsVersion()));
        return new PluginInstancesRepositoryWhichCannotBeClosed(config);
    }

    private static Map<String, SonarLintRuleDefinition> computeAllRulesDefinitionsByKey() {
        return loadPluginMetadata(
                pluginInstancesRepository, globalConfig.getEnabledLanguages(), false);
    }

    private static AnalysisEngineConfiguration buildAnalysisEngineConfiguration() {
        return AnalysisEngineConfiguration.builder()
                .addEnabledLanguages(globalConfig.getEnabledLanguages())
                .setClientPid(globalConfig.getClientPid())
                .setExtraProperties(globalConfig.extraProperties())
                .setWorkDir(globalConfig.getWorkDir())
                .setModulesProvider(globalConfig.getModulesProvider())
                .build();
    }

    /** Get or creates the one and only instance of this class. */
    public static SonarLintEngine getInstance() {
        if (theOnlyInstance == null) {
            theOnlyInstance = new SonarLintEngine();
        } else {
            theOnlyInstance.recreateAnalysisEngine();
        }
        return theOnlyInstance;
    }

    /**
     * Recreates the analysis engine as it is stopped after each analysis executed by {@link
     * SonarStaticAnalyzer}.
     */
    public void recreateAnalysisEngine() {
        this.analysisEngine =
                new AnalysisEngine(analysisGlobalConfig, pluginInstancesRepository, logOutput);
    }

    @Override
    public AnalysisEngine getAnalysisEngine() {
        return analysisEngine;
    }

    public AnalysisResults analyze(
            StandaloneAnalysisConfiguration configuration,
            CommandConfiguration soraldConfiguration,
            IssueListener issueListener,
            @Nullable ClientLogOutput logOutput,
            @Nullable ClientProgressMonitor monitor) {
        requireNonNull(configuration);
        requireNonNull(issueListener);
        setLogging(logOutput);

        var analysisConfig =
                AnalysisConfiguration.builder()
                        .addInputFiles(configuration.inputFiles())
                        .putAllExtraProperties(configuration.extraProperties())
                        .addActiveRules(identifyActiveRules(soraldConfiguration))
                        .setBaseDir(configuration.baseDir())
                        .build();
        try {
            var analysisResults =
                    analysisEngine
                            .post(
                                    new AnalyzeCommand(
                                            configuration.moduleKey(),
                                            analysisConfig,
                                            i ->
                                                    issueListener.handle(
                                                            new DefaultClientIssue(
                                                                    i,
                                                                    allRulesDefinitionsByKey.get(
                                                                            i.getRuleKey()))),
                                            logOutput),
                                    new ProgressMonitor(monitor))
                            .get();
            return analysisResults == null ? new AnalysisResults() : analysisResults;
        } catch (ExecutionException e) {
            throw SonarLintWrappedException.wrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SonarLintWrappedException.wrap(e);
        }
    }

    private Collection<ActiveRule> identifyActiveRules(CommandConfiguration soraldConfiguration) {
        if (soraldConfiguration.getRules() != null) {
            return allRulesDefinitionsByKey.values().stream()
                    .filter(isImplementedBySonarJavaPlugin(soraldConfiguration.getRules()))
                    .map(rd -> new ActiveRule(rd.getKey(), rd.getLanguage().getLanguageKey()))
                    .collect(toSet());
        }

        if (soraldConfiguration.onlyHandledRules() != null
                && soraldConfiguration.getRuleTypes() != null) {
            List<SonarLintRuleDefinition> requiredRules;

            if (soraldConfiguration.getRuleTypes().isEmpty()) {
                requiredRules = List.copyOf(allRulesDefinitionsByKey.values());
            } else {
                List<String> ruleTypes =
                        soraldConfiguration.getRuleTypes().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList());
                requiredRules =
                        List.copyOf(
                                allRulesDefinitionsByKey.values().stream()
                                        .filter(slrd -> ruleTypes.contains(slrd.getType()))
                                        .collect(Collectors.toList()));
            }

            return !soraldConfiguration.onlyHandledRules()
                    ? requiredRules.stream()
                            .map(
                                    rd ->
                                            new ActiveRule(
                                                    rd.getKey(), rd.getLanguage().getLanguageKey()))
                            .collect(toSet())
                    : requiredRules.stream()
                            .filter(
                                    rule ->
                                            Processors.getProcessor(rule.getKey().substring(5))
                                                    != null)
                            .map(
                                    rd ->
                                            new ActiveRule(
                                                    rd.getKey(), rd.getLanguage().getLanguageKey()))
                            .collect(Collectors.toList());
        }

        return allRulesDefinitionsByKey.values().stream()
                .map(rd -> new ActiveRule(rd.getKey(), rd.getLanguage().getLanguageKey()))
                .collect(Collectors.toList());
    }

    private static Predicate<? super SonarLintRuleDefinition> isImplementedBySonarJavaPlugin(
            List<Rule> rules) {
        return r ->
                rules.stream()
                        .map(rule -> RuleKey.parse("java:" + rule.getKey()))
                        .anyMatch(providedRule -> providedRule.toString().equals(r.getKey()));
    }

    public void stop() {
        analysisEngine.stop();
    }

    public List<PluginDetails> getPluginDetails() {
        throw new RuntimeException("Not implemented"); // NOSONAR:S112
    }

    /**
     * Overriding this class to ensure its instance never closes throughout the lifecycle of JVM.
     */
    public static class PluginInstancesRepositoryWhichCannotBeClosed
            extends PluginInstancesRepository {

        public PluginInstancesRepositoryWhichCannotBeClosed(Configuration configuration) {
            super(configuration);
        }

        @Override
        public void close() throws Exception {
            // Prevent closing of instance of this class
        }
    }
}
