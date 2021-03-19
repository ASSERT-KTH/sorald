package sorald.processor;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import sorald.Constants;
import sorald.Main;
import sorald.TestHelper;
import sorald.sonar.RuleVerifier;

public class NoSonarTest {
    @Test
    public void noSonarTesting() throws Exception {
        String fileName = "NOSONARCommentTest.java";
        Path pathToBuggyFile = TestHelper.PATH_TO_RESOURCES_FOLDER.resolve(fileName);
        String pathToRepairedFile =
                TestHelper.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

        RuleVerifier.verifyHasIssue(
                pathToBuggyFile.toString(), new ArrayHashCodeAndToStringCheck());
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    pathToBuggyFile.toString(),
                    Constants.ARG_RULE_KEY,
                    "2116",
                    Constants.ARG_MAX_FIXES_PER_RULE,
                    "3"
                });
        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyHasIssue(
                pathToRepairedFile, new ArrayHashCodeAndToStringCheck()); // one bug left
    }
}
