package sorald;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import sorald.sonar.RuleVerifier;

public class MultipleProcessorsTest {

    @Test
    public void test_threeExistingRules() throws Exception {
        String fileName = "MultipleProcessors.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile =
                Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

        Main.main(
                new String[] {
                        Constants.REPAIR_COMMAND_NAME,
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
