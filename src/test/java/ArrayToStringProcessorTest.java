import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class ArrayToStringProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "ArrayToString.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new ArrayHashCodeAndToStringCheck());
        TestHelp.normalRepair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,2116);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

}