package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.MathOnFloatCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class MathOnFloatProcessorTest {

    @Test
    public void test() throws Exception {
        String fileName = "MathOnFloat.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FILE + fileName;
        String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new MathOnFloatCheck());
        Main.main(new String[]{
                "--originalFilesPath",pathToBuggyFile,
                "--ruleKeys","2164",
                "--prettyPrintingStrategy","SNIPER",
                "--workspace",Constants.WORKSPACE});
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new MathOnFloatCheck());
    }

}
