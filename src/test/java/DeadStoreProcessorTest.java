import org.junit.Test;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class DeadStoreProcessorTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void DeadStore()throws Exception
    {
        String fileName = "DeadStores.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName, new DeadStoreCheck());
        TestHelp.normalRepair(pathToFile,projectKey,1854);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new DeadStoreCheck());
    }

}