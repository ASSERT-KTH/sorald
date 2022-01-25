package sorald.sonar;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

public final class SonarLintEngine extends AbstractSonarLintEngine {

    // The order of these initialisations is important as each field is dependent upon the previous
    // one.
    private static final Path sonarJavaPath =
            Paths.get("target/classes").resolve("sonar-java-plugin-6.12.0.24852.jar");
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

    private static StandaloneGlobalConfiguration buildGlobalConfig() {
        return StandaloneGlobalConfiguration.builder()
                .addPlugin(sonarJavaPath)
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
                        .addActiveRules(identifyActiveRules(configuration))
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
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw SonarLintWrappedException.wrap(e);
        }
    }

    private Collection<ActiveRule> identifyActiveRules(
            StandaloneAnalysisConfiguration configuration) {
        Set<String> includedRules =
                configuration.includedRules().stream().map(RuleKey::toString).collect(toSet());

        return allRulesDefinitionsByKey.values().stream()
                .filter(isImplementedBySonarJavaPlugin(includedRules))
                .map(rd -> new ActiveRule(rd.getKey(), rd.getLanguage().getLanguageKey()))
                .collect(Collectors.toList());
    }

    private static Predicate<? super SonarLintRuleDefinition> isImplementedBySonarJavaPlugin(
            Set<String> includedRules) {
        return r -> includedRules.contains(r.getKey());
    }

    public void stop() {
        analysisEngine.stop();
    }

    public List<PluginDetails> getPluginDetails() {
        return pluginInstancesRepository.getPluginCheckResultByKeys().values().stream()
                .map(
                        c ->
                                new PluginDetails(
                                        c.getPlugin().getKey(),
                                        c.getPlugin().getName(),
                                        c.getPlugin().getVersion().toString(),
                                        c.getSkipReason().orElse(null)))
                .collect(Collectors.toList());
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
