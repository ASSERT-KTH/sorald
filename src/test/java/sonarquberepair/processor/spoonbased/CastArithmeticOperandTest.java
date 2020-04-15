package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.PrettyPrintingStrategy;
import sonarquberepair.TestHelper;

public class CastArithmeticOperandTest {

    @Test
    public void test() throws Exception {
        String fileName = "CastArithmeticOperand.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = Constants.WORKSPACE + "/spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new CastArithmeticOperandCheck());
        Main.main(new String[]{
            "--originalFilesPath",pathToBuggyFile,
            "--projectKey",Constants.PROJECT_KEY,
            "--ruleKeys","2184",
            "--prettyPrintingStrategy","SNIPER",
            "--workspace",Constants.WORKSPACE});
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
    }

}
