package sorald.sonar;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.Plugin;
import org.sonarsource.sonarlint.core.AbstractSonarLintEngine;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.analysis.api.ActiveRule;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisEngineConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.analysis.command.AnalyzeCommand;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.exceptions.SonarLintWrappedException;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.commons.RuleKey;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.plugin.commons.LoadedPlugins;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoadResult;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoader;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoader.Configuration;
import org.sonarsource.sonarlint.core.plugin.commons.loading.PluginInfo;
import org.sonarsource.sonarlint.core.plugin.commons.loading.PluginInstancesLoader;
import org.sonarsource.sonarlint.core.plugin.commons.loading.PluginRequirementsCheckResult;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.FileUtils;
import sorald.util.ConfigLoader;

public final class SonarLintEngine extends AbstractSonarLintEngine {

    // The order of these initialisations is important as each field is dependent upon the previous
    // one.
    private static final String SONAR_JAVA_PLUGIN_JAR_NAME = "sonar-java-plugin.jar";
    private static final Path sonarJavaPlugin = getOrDownloadSonarJavaPlugin().getPath();
    private static final StandaloneGlobalConfiguration globalConfig = buildGlobalConfig();
    private static final LoadedPluginsThatDoesNotCloseLoader loadedPlugins = getLoadedPlugins();
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

        this.analysisEngine = new AnalysisEngine(analysisGlobalConfig, loadedPlugins, null);
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
        } catch (IOException e) {
            throw new RuntimeException("Could not download Sonar Java plugin", e); // NOSONAR:S112
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

    private static LoadedPluginsThatDoesNotCloseLoader getLoadedPlugins() {
        var config =
                new Configuration(
                        globalConfig.getPluginPaths(),
                        globalConfig.getEnabledLanguages(),
                        Optional.ofNullable(globalConfig.getNodeJsVersion()));

        PluginsLoadResult loadedResult = new PluginsLoader().load(config);
        // Default loaded result stops the loader. The following code prevents that.

        Map<String, PluginRequirementsCheckResult> pluginCheckResultByKeys =
                loadedResult.getPluginCheckResultByKeys();
        Collection<PluginInfo> allPlugins = getAllPlugins(pluginCheckResultByKeys);

        // We do not want this loader to close.
        PluginInstancesLoader instancesLoader = new PluginInstancesLoader();
        Map<String, Plugin> pluginInstancesByKeys =
                instancesLoader.instantiatePluginClasses(allPlugins);

        return new LoadedPluginsThatDoesNotCloseLoader(
                pluginInstancesByKeys, new PluginInstancesLoader());
    }

    private static Collection<PluginInfo> getAllPlugins(
            Map<String, PluginRequirementsCheckResult> pluginCheckResultByKeys) {
        return pluginCheckResultByKeys.values().stream()
                .map(PluginRequirementsCheckResult::getPlugin)
                .collect(toList());
    }

    private static Map<String, SonarLintRuleDefinition> computeAllRulesDefinitionsByKey() {
        return loadPluginMetadata(loadedPlugins, globalConfig.getEnabledLanguages(), false, false);
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
        this.analysisEngine = new AnalysisEngine(analysisGlobalConfig, loadedPlugins, logOutput);
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
        } catch (ExecutionException e) {
            throw SonarLintWrappedException.wrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SonarLintWrappedException.wrap(e);
        }
    }

    /** Returns all rule keys available at the provided version of Sonar Java. */
    public static Map<String, SonarLintRuleDefinition> getAllRulesDefinitionsByKey() {
        return allRulesDefinitionsByKey;
    }

    private Collection<ActiveRule> identifyActiveRules(
            StandaloneAnalysisConfiguration configuration) {
        Set<String> includedRules =
                configuration.includedRules().stream().map(RuleKey::toString).collect(toSet());

        return allRulesDefinitionsByKey.values().stream()
                .filter(isImplementedBySonarJavaPlugin(includedRules))
                .map(
                        rd -> {
                            ActiveRule activeRule =
                                    new ActiveRule(rd.getKey(), rd.getLanguage().getLanguageKey());
                            RuleKey ruleKey = RuleKey.parse(rd.getKey());
                            if (configuration.ruleParameters().containsKey(ruleKey)) {
                                activeRule.setParams(configuration.ruleParameters().get(ruleKey));
                            }
                            return activeRule;
                        })
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
        throw new RuntimeException("Not implemented"); // NOSONAR:S112
    }

    /**
     * Overriding this class to ensure that plugin instance loader never closes throughout the
     * lifecycle of JVM.
     */
    public static class LoadedPluginsThatDoesNotCloseLoader extends LoadedPlugins {

        public LoadedPluginsThatDoesNotCloseLoader(
                Map<String, Plugin> pluginInstancesByKeys,
                PluginInstancesLoader pluginInstancesLoader) {
            super(pluginInstancesByKeys, pluginInstancesLoader);
        }

        @Override
        public void unload() {
            // Prevent closing of `pluginInstancesLoader`
        }
    }
}
