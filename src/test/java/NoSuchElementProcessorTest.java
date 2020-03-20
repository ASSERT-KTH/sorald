import org.junit.Test;
import org.sonar.java.checks.IteratorNextExceptionCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class NoSuchElementProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "NoSuchElement.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new IteratorNextExceptionCheck());
        TestHelp.normalRepair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,2272);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new IteratorNextExceptionCheck());
    }
}