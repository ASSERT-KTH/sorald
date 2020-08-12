package sorald.processor;

import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;

public class SegmentStrategyTest {
    @Test
    public void arrayToStringProcessor_success_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/SEGMENT/" + Constants.SPOONED + "/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        Main.main(new String[]{
                Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY, "SEGMENT",
                Constants.ARG_SYMBOL + Constants.ARG_MAX_FILES_PER_SEGMENT, "3",
                Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH, Constants.PATH_TO_RESOURCES_FOLDER,
                Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS, "2116",
                Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.NORMAL.name(),
                Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, Constants.SORALD_WORKSPACE + "/SEGMENT/"});
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

    @Test(expected = RuntimeException.class)
    public void arrayToStringProcessor_fail_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/SEGMENT/" + Constants.SPOONED + "/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        Main.main(new String[]{
                Constants.ARG_SYMBOL + Constants.ARG_REPAIR_STRATEGY, "SEGMENT",
                Constants.ARG_SYMBOL + Constants.ARG_MAX_FILES_PER_SEGMENT, "0",
                Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH, Constants.PATH_TO_RESOURCES_FOLDER,
                Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS, "2116",
                Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.NORMAL.name(),
                Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, Constants.SORALD_WORKSPACE + "/SEGMENT/"});
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }
}
