import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.UnclosedResourcesCheck;

import static org.junit.Assert.*;

public class ResourceCloseProcessorTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test() throws Exception{

        String fileName = "ZipFolder.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName, new UnclosedResourcesCheck());
        TestHelp.normalRepair(pathToFile + fileName,projectKey,2095);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new UnclosedResourcesCheck());

    }
}