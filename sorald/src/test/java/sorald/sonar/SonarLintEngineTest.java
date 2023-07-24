package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;

class SonarLintEngineTest {

    private static final List<SonarRule> DEACTIVATED_RULES =
            Stream.of("java:S1943").map(SonarRule::new).collect(Collectors.toList());

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

    @Test
    void DEACTIVATED_RULES_shouldBeDeactivatedByDefault() {
        // arrange
        Map<String, SonarLintRuleDefinition> allRules =
                SonarLintEngine.getAllRulesDefinitionsByKey();
        List<SonarRule> deactivatedRules = getDeactivatedRules(allRules);

        // assert
        assertThat(deactivatedRules, hasItems(DEACTIVATED_RULES.toArray(new SonarRule[0])));
    }

    private static List<SonarRule> getDeactivatedRules(
            Map<String, SonarLintRuleDefinition> sonarLintEngine) {
        return sonarLintEngine.values().stream()
                .filter(rule -> !rule.isActiveByDefault())
                .map(rule -> new SonarRule(rule.getKey()))
                .collect(Collectors.toList());
    }
}
