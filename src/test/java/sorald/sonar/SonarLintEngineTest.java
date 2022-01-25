package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.commons.Language;

public class SonarLintEngineTest {
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

    @Nested
    class SonarJavaPlugin {
        @Test
        void exactlyOneSonarJavaPluginIsConfiguredWithSonarLintEngine() {
            // arrange
            List<PluginDetails> pluginDetails = SonarLintEngine.getInstance().getPluginDetails();

            // assert
            assertThat(pluginDetails.size(), equalTo(1));
        }

        @Test
        void languageAndVersionOfPluginShouldMatchThatOfResource() {
            // arrange
            List<PluginDetails> pluginDetails = SonarLintEngine.getInstance().getPluginDetails();
            PluginDetails plugin = pluginDetails.get(0);
            File resourceDirectory = new File("target/classes");
            File sonarJavaPlugin =
                    Arrays.stream(
                                    resourceDirectory.listFiles(
                                            f -> f.getName().contains("sonar-java-plugin")))
                            .findFirst()
                            .get();

            // assert
            assertThat(plugin.key(), equalTo(Language.JAVA.getLanguageKey()));
            assertThat(
                    "sonar-java-plugin-" + plugin.version() + ".jar",
                    equalTo(sonarJavaPlugin.getName()));
        }
    }
}
