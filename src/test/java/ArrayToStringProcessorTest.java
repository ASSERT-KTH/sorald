import org.junit.Test;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class ArrayToStringProcessorTest {

    @Test
    public void arrayToStringProcessorTest()throws Exception
    {
        String fileName = "ArrayToString.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        TestHelp.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,2116);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

    @Test
    public void arrayToStringProcessorTest2()throws Exception
    {
        String fileName = "CodeFactory.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/spoon/reflect/factory/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        TestHelp.repair(pathToBuggyFile,Constants.PROJECT_KEY,2116);
        TestHelp.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

}