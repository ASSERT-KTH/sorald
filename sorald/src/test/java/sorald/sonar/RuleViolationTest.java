package sorald.sonar;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import sorald.TestHelper;
import sorald.processor.XxeProcessingProcessor;
import sorald.rule.RuleViolation;

import java.io.File;

public class RuleViolationTest {
    @Test
    public void equals_onNonRuleViolationType_returnsFalse() {
        File resources = TestHelper.PATH_TO_RESOURCES_FOLDER.toFile();
        RuleViolation violation =
                ProjectScanner.scanProject(
                                resources,
                                resources,
                                new SonarRule(new XxeProcessingProcessor().getRuleKey()))
                        .stream()
                        .findFirst()
                        .get();

        assertNotEquals(violation, 2);
    }
}
