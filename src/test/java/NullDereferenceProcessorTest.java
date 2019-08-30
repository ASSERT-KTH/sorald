/**
 * DISABLED as per commit b965cad6f327d8fd0fb97a3af8f6427de61685c4
 * The processor is non-functioning. See commit message above for more info.
 */
/*
import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.NullDereferenceCheck;

public class NullDereferenceProcessorTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test()throws Exception
    {
        String fileName = "NullDereferences.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName,new NullDereferenceCheck());
        TestHelp.normalRepair(pathToFile + fileName,projectKey,2259);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new NullDereferenceCheck());
    }

}
*/