import org.junit.Test;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class CastArithmeticOperandTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test()throws Exception {
        String fileName = "CastArithmeticOperand.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName, new CastArithmeticOperandCheck());
        TestHelp.repair(pathToFile + fileName, projectKey,2184);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
    }

}
