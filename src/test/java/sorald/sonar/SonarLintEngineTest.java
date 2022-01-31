package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;

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
}
