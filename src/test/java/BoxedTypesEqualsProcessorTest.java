import org.junit.Test;
import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class BoxedTypesEqualsProcessorTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test()throws Exception
    {
        String fileName = "BoxedTypesEquals.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName, new CompareStringsBoxedTypesWithEqualsCheck());
        TestHelp.normalRepair(pathToFile,projectKey,4973);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CompareStringsBoxedTypesWithEqualsCheck());
    }

}