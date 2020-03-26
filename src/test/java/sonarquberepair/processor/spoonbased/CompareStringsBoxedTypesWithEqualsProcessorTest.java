package sonarquberepair.processor.spoonbased;

import org.junit.Test;
import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.TestHelper;

public class CompareStringsBoxedTypesWithEqualsProcessorTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "CompareStringsBoxedTypesWithEquals.java";
        String pathToBuggyFile = Constants.PATH_TO_FILE + fileName;
        String pathToRepairedFile = "./spooned/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new CompareStringsBoxedTypesWithEqualsCheck());
        Main.normalRepair(pathToBuggyFile,Constants.PROJECT_KEY,4973);
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CompareStringsBoxedTypesWithEqualsCheck());
    }

}