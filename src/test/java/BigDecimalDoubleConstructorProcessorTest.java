import org.junit.Test;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class BigDecimalDoubleConstructorProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "BigDecimalDoubleConstructor.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new BigDecimalDoubleConstructorCheck());
        TestHelp.repair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,2111);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new BigDecimalDoubleConstructorCheck());
    }

}