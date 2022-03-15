package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.analysis.api.ActiveRule;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.Processors;
import sorald.cli.CommandConfiguration;
import sorald.rule.RuleType;

class SonarLintEngineTest {
    @Test
    void sonarLintEngineReinitializesAnalysisEngineForEachExecution() {
        // arrange
        Set<AnalysisEngine> analysisEngines = new HashSet<>();
        int numberOfExecutions = 10;

        // act
        for (int i = 0; i < numberOfExecutions; ++i) {
            SonarLintEngine sonarLintEngine = SonarLintEngine.getInstance();
            analysisEngines.add(sonarLintEngine.getAnalysisEngine());
        }

        // assert
        assertThat(analysisEngines.size(), equalTo(numberOfExecutions));
    }

    @Test
    void getOrDownloadSonarJavaPlugin_sonarJavaJarShouldBeFetchedFromCache()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException {
        // arrange
        Class<?> sonarLintEngine = Class.forName("sorald.sonar.SonarLintEngine");
        Method method = sonarLintEngine.getDeclaredMethod("getOrDownloadSonarJavaPlugin");
        method.setAccessible(true);

        // act
        // The first invocation may download or fetch it from cache
        method.invoke(null);
        // The second invocation must get it from cache
        SonarLintEngine.SonarJavaJarHolder result =
                (SonarLintEngine.SonarJavaJarHolder) method.invoke(null);

        // assert
        assertThat(result.getPath().toFile(), anExistingFile());
        assertFalse(result.isDownloaded());
    }

    @Nested
    class CorrectSubsetOfRulesIsMarkedActive {
        private Collection<ActiveRule> arrangeAndActIdentifyActiveRules(
                CommandConfiguration soraldConfiguration)
                throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                        IllegalAccessException {
            // arrange
            Class<?> sonarLintEngine = Class.forName("sorald.sonar.SonarLintEngine");
            Method method =
                    sonarLintEngine.getDeclaredMethod(
                            "identifyActiveRules", CommandConfiguration.class);
            method.setAccessible(true);

            // act
            return (Collection<ActiveRule>) method.invoke(null, soraldConfiguration);
        }

        @Test
        void identifyActiveRules_subsetOfRulesShouldHaveCorrectRuleType()
                throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                        IllegalAccessException {

            Collection<ActiveRule> activeRules =
                    arrangeAndActIdentifyActiveRules(
                            new CommandConfiguration(false, List.of(RuleType.VULNERABILITY)));

            // assert
            // Check if the active rule is valid and has the correct type.
            activeRules.forEach(
                    activeRule -> {
                        SonarLintRuleDefinition slrd =
                                SonarLintEngine.getAllRulesDefinitionsByKey()
                                        .get(activeRule.getRuleKey());
                        assertThat(slrd, is(notNullValue()));
                        assertThat(slrd.getType(), is(RuleType.VULNERABILITY.name()));
                    });
        }

        @Test
        void identifyActiveRules_subsetOfRulesShouldBeHandledBySorald()
                throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
                        IllegalAccessException {
            Collection<ActiveRule> activeRules =
                    arrangeAndActIdentifyActiveRules(new CommandConfiguration(true, List.of()));

            // assert
            // Check if all active rules have a corresponding processor.
            assertThat(activeRules.size(), equalTo(Processors.getAllProcessors().size()));
            activeRules.forEach(
                    activeRule ->
                            assertThat(
                                    Processors.getProcessor(activeRule.getRuleKey().substring(5)),
                                    is(notNullValue())));
        }
    }
}
