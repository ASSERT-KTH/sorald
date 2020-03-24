import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.UnclosedResourcesCheck;

public class UnclosedResourcesProcessorTest {

    @Test
    public void test() throws Exception{

        String fileName = "ZipFolder.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new UnclosedResourcesCheck());
        TestHelp.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,2095);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnclosedResourcesCheck());

    }
}