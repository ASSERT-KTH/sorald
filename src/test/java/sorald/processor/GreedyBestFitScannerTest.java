package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sorald.Constants;
import sorald.sonar.Checks;
import sorald.sonar.GreedyBestFitScanner;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class GreedyBestFitScannerTest {

    @Test
    public void calculateBestFits_throws_whenProcessorConcernsDifferentRuleThanViolations() {
        File projectBaseDir = new File(Constants.PATH_TO_RESOURCES_FOLDER);
        Set<RuleViolation> xxeProcessingViolations =
                ProjectScanner.scanProject(
                        projectBaseDir,
                        projectBaseDir,
                        Checks.getCheckInstance(new XxeProcessingProcessor().getRuleKey()));
        SoraldAbstractProcessor<?> arrayHashCodeProc = new ArrayHashCodeAndToStringProcessor();

        Launcher launcher = new Launcher();
        launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
        launcher.getEnvironment().setComplianceLevel(Constants.DEFAULT_COMPLIANCE_LEVEL);
        launcher.addInputResource(projectBaseDir.toString());
        CtModel model = launcher.buildModel();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        GreedyBestFitScanner.calculateBestFits(
                                model.getUnnamedModule(),
                                xxeProcessingViolations,
                                arrayHashCodeProc));
    }
}
