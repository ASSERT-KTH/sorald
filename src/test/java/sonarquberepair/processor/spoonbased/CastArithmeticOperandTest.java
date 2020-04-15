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
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new CastArithmeticOperandCheck());
        Main.repair(pathToBuggyFile, Constants.PROJECT_KEY, 2184, PrettyPrintingStrategy.SNIPER);
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
    }

}
