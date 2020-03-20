import org.junit.Test;
import org.sonar.java.checks.IteratorNextExceptionCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class NoSuchElementProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "NoSuchElement.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new IteratorNextExceptionCheck());
        TestHelp.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,2272);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new IteratorNextExceptionCheck());
    }
}