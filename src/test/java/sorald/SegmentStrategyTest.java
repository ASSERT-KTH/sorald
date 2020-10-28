package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;
import sorald.sonar.RuleVerifier;

public class SegmentStrategyTest {
    @Test
    public void arrayToStringProcessor_success_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile =
                Constants.SORALD_WORKSPACE + "/SEGMENT/" + Constants.SPOONED + "/" + fileName;

        RuleVerifier.verifyHasIssue(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        Main.main(
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY,
                    "SEGMENT",
                    // FIXME MAX_FILES_PER_SEGMENT is set to 1 as a temporary fix to
                    // https://github.com/SpoonLabs/sorald/issues/154
                    Constants.ARG_SYMBOL + Constants.ARG_MAX_FILES_PER_SEGMENT,
                    "1",
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "2116",
                    Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    PrettyPrintingStrategy.NORMAL.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE + "/SEGMENT/"
                });
        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

    @Test
    public void arrayToStringProcessor_fail_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;

        RuleVerifier.verifyHasIssue(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        String[] args =
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY,
                    "SEGMENT",
                    Constants.ARG_SYMBOL + Constants.ARG_MAX_FILES_PER_SEGMENT,
                    "0",
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "2116",
                    Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    PrettyPrintingStrategy.NORMAL.name(),
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE + "/SEGMENT/"
                };
        assertThrows(RuntimeException.class, () -> Main.main(args));
    }

    @Test
    public void segmentStrategy_repairsAllIssues_whenMultipleRulesAreViolated() throws Exception {
        String fileName = "MultipleProcessors.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile =
                Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

        Main.main(
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY,
                    "SEGMENT",
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    pathToBuggyFile,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    "2111,2184,2204",
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE
                });

        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new EqualsOnAtomicClassCheck());
    }
}
