import org.junit.Test;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class DeadStoreProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "DeadStores.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new DeadStoreCheck());
        TestHelp.normalRepair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,1854);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new DeadStoreCheck());
    }

}