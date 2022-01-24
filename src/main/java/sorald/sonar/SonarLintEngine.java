package sorald.sonar;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository.Configuration;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;

public final class SonarLintEngine extends AbstractSonarLintEngine
        implements StandaloneSonarLintEngine {

    private final StandaloneGlobalConfiguration globalConfig;
    private final Collection<PluginDetails> pluginDetails;
    private final Map<String, SonarLintRuleDefinition> allRulesDefinitionsByKey;
    private AnalysisEngine analysisEngine;

    private static PluginInstancesRepositoryWhichCannotBeClosed pluginInstancesRepository;
    private final AnalysisEngineConfiguration analysisGlobalConfig;

    public SonarLintEngine(StandaloneGlobalConfiguration globalConfig) {
        super(globalConfig.getLogOutput());
        this.globalConfig = globalConfig;
        setLogging(null);

        pluginInstancesRepository = createPluginInstancesRepository();
        pluginDetails =
                pluginInstancesRepository.getPluginCheckResultByKeys().values().stream()
                        .map(
                                c ->
                                        new PluginDetails(
                                                c.getPlugin().getKey(),
                                                c.getPlugin().getName(),
                                                c.getPlugin().getVersion().toString(),
                                                c.getSkipReason().orElse(null)))
                        .collect(Collectors.toList());

        allRulesDefinitionsByKey =
                loadPluginMetadata(
                        pluginInstancesRepository, globalConfig.getEnabledLanguages(), false);

        analysisGlobalConfig =
                AnalysisEngineConfiguration.builder()
                        .addEnabledLanguages(globalConfig.getEnabledLanguages())
                        .setClientPid(globalConfig.getClientPid())
                        .setExtraProperties(globalConfig.extraProperties())
                        .setNodeJs(globalConfig.getNodeJsPath())
                        .setWorkDir(globalConfig.getWorkDir())
                        .setModulesProvider(globalConfig.getModulesProvider())
                        .build();
        this.analysisEngine =
                new AnalysisEngine(analysisGlobalConfig, createPluginInstancesRepository(), logOutput);
    }

    public void recreateAnalysisEngine() {
        this.analysisEngine = new AnalysisEngine(analysisGlobalConfig, pluginInstancesRepository, logOutput);
    }

    @Override
    public AnalysisEngine getAnalysisEngine() {
        return analysisEngine;
    }

    private PluginInstancesRepositoryWhichCannotBeClosed createPluginInstancesRepository() {
        var config =
                new Configuration(
                        globalConfig.getPluginPaths(),
                        globalConfig.getEnabledLanguages(),
                        Optional.ofNullable(globalConfig.getNodeJsVersion()));
        return new PluginInstancesRepositoryWhichCannotBeClosed(config);
    }

    @Override
    public Optional<StandaloneRuleDetails> getRuleDetails(String ruleKey) {
        return Optional.ofNullable(allRulesDefinitionsByKey.get(ruleKey)).map(StandaloneRule::new);
    }

    @Override
    public Collection<StandaloneRuleDetails> getAllRuleDetails() {
        return allRulesDefinitionsByKey.values().stream()
                .map(StandaloneRule::new)
                .collect(Collectors.toList());
    }

    @Override
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
        } catch (Exception e) {
            throw SonarLintWrappedException.wrap(e);
        }
    }

    private Collection<ActiveRule> identifyActiveRules(
            StandaloneAnalysisConfiguration configuration) {
        Set<String> excludedRules =
                configuration.excludedRules().stream().map(RuleKey::toString).collect(toSet());
        Set<String> includedRules =
                configuration.includedRules().stream()
                        .map(RuleKey::toString)
                        .filter(r -> !excludedRules.contains(r))
                        .collect(toSet());

        Collection<SonarLintRuleDefinition> filteredActiveRules =
                allRulesDefinitionsByKey.values().stream()
                        .filter(isIncludedByConfiguration(includedRules))
                        .collect(Collectors.toList());

        return filteredActiveRules.stream()
                .map(
                        rd -> {
                            var activeRule =
                                    new ActiveRule(rd.getKey(), rd.getLanguage().getLanguageKey());
                            Map<String, String> effectiveParams =
                                    new HashMap<>(rd.getDefaultParams());
                            Optional.ofNullable(
                                            configuration
                                                    .ruleParameters()
                                                    .get(RuleKey.parse(rd.getKey())))
                                    .ifPresent(effectiveParams::putAll);
                            activeRule.setParams(effectiveParams);
                            return activeRule;
                        })
                .collect(Collectors.toList());
    }

    private static Predicate<? super SonarLintRuleDefinition> isIncludedByConfiguration(
            Set<String> includedRules) {
        return r -> {
            if (includedRules.contains(r.getKey())) {
                return true;
            }
            for (String deprecatedKey : r.getDeprecatedKeys()) {
                if (includedRules.contains(deprecatedKey)) {
                    LOG.warn(
                            "Rule '{}' was included using its deprecated key '{}'. Please fix your configuration.",
                            r.getKey(),
                            deprecatedKey);
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public void stop() {
        analysisEngine.stop();
    }

    @Override
    public Collection<PluginDetails> getPluginDetails() {
        return pluginDetails;
    }

    public static class PluginInstancesRepositoryWhichCannotBeClosed extends PluginInstancesRepository {

        public PluginInstancesRepositoryWhichCannotBeClosed(Configuration configuration) {
            super(configuration);
        }

        @Override
        public void close() throws Exception { }
    }
}
