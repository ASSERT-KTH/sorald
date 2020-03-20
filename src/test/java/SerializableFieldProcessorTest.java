import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class SerializableFieldProcessorTest {

    @Test
    public void test() throws Exception{

        String fileName = "SerializableFieldProcessorTest.java";
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(Constants.PATH_TO_FILE + fileName, new SerializableFieldInSerializableClassCheck());
        TestHelp.normalRepair(Constants.PATH_TO_FILE + fileName,Constants.PROJECT_KEY,1948);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SerializableFieldInSerializableClassCheck());
    }

}