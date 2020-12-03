package sorald.processor;

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
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile =
                Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

        RuleVerifier.verifyHasIssue(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    pathToBuggyFile,
                    Constants.ARG_RULE_KEYS,
                    "2116",
                    Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE,
                    Constants.ARG_MAX_FIXES_PER_RULE,
                    "3"
                });
        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyHasIssue(
                pathToBuggyFile, new ArrayHashCodeAndToStringCheck()); // one bug left
    }
}
