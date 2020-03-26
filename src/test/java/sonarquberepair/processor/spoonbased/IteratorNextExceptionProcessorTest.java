package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.IteratorNextExceptionCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class IteratorNextExceptionProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "IteratorNextException.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new IteratorNextExceptionCheck());
        Main.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,2272);
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new IteratorNextExceptionCheck());
    }
}