import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class SerializableFieldProcessorTest {

    private static String projectKey = "se.kth:sonatest";
    private static String cdtest ="./src/test/resources/sonatest/";
    private static String pathToFile = cdtest + "src/main/java/";

    @Test
    public void test() throws Exception{

        String fileName = "SerializableFieldProcessorTest.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToFile + fileName, new SerializableFieldInSerializableClassCheck());
        TestHelp.normalRepair(pathToFile + fileName,projectKey,1948);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SerializableFieldInSerializableClassCheck());
    }

}