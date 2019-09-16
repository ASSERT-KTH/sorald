import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class CodeFactoryTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test()throws Exception
    {
        String fileName = "CodeFactory.java";
        String pathToRepairedFile = "./spooned/spoon/reflect/factory/" + fileName;
        TestHelp.normalRepair(pathToFile,projectKey,2116);
        TestHelp.repair(pathToFile,projectKey,2116);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }
}