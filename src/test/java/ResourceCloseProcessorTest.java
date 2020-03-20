import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.UnclosedResourcesCheck;

public class ResourceCloseProcessorTest {

    @Test
    public void test() throws Exception{

        String fileName = "ZipFolder.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new UnclosedResourcesCheck());
        TestHelp.normalRepair(Constants.PATH_TO_FILE + fileName,Constants.PROJECT_KEY,2095);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnclosedResourcesCheck());

    }
}