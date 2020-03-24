import org.junit.Test;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class SerializableFieldInSerializableClassProcessorTest {

    @Test
    public void test() throws Exception{

        String fileName = "SerializableFieldProcessorTest.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new SerializableFieldInSerializableClassCheck());
        TestHelp.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,1948);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new SerializableFieldInSerializableClassCheck());
    }

}