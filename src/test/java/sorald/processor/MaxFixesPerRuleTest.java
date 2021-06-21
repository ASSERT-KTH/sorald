package sorald.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import sorald.Constants;
import sorald.Main;
import sorald.TestHelper;
import sorald.sonar.RuleVerifier;

public class MaxFixesPerRuleTest {
    @Test
    public void arrayToStringProcessorTest() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        Path pathToBuggyFile = TestHelper.createTemporaryTestResourceWorkspace().resolve(fileName);

        RuleVerifier.verifyHasIssue(
                pathToBuggyFile.toString(), new ArrayHashCodeAndToStringCheck());
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_SOURCE,
                    pathToBuggyFile.toString(),
                    Constants.ARG_RULE_KEY,
                    new ArrayHashCodeAndToStringProcessor().getRuleKey(),
                    Constants.ARG_MAX_FIXES_PER_RULE,
                    "3"
                });
        TestHelper.removeComplianceComments(pathToBuggyFile.toString());

        try {
            RuleVerifier.verifyHasIssue(
                    pathToBuggyFile.toString(), new ArrayHashCodeAndToStringCheck());
        } catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("Unexpected at [27]"));
        }
    }
}
