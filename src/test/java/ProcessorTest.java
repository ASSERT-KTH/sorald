import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ProcessorTest {

    private static String projectKey = "se.kth:sonatest";
/*
    @BeforeAll
    static void updateSonatestAnalysis()
    {
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";
        TestHelp.copy(cdtest+"src",cdrep+"src");
        TestHelp.doSonarAnalysis(cdrep);
        TestHelp.doSonarAnalysis(cdtest);
    }
*/
    @Test
    void DeadStore()throws Exception
    {
        String cdtest = "./src/test/sonatest/";
        String pathToFile = cdtest + "src/main/java/DeadStores.java";
        String pathToRepairedFile = "./spooned/DeadStores.java";

        assertTrue(TestHelp.hasSonarBug(pathToFile,1854));
        TestHelp.repair(pathToFile,projectKey,1854);
        assertFalse(TestHelp.hasSonarBug(pathToRepairedFile,1854));
    }

    @Test
    void NullDereference()throws Exception
    {
        String cdtest = "./src/test/sonatest/";
        String pathToFile = cdtest + "src/main/java/NullDereferences.java";
        String pathToRepairedFile = "./spooned/NullDereferences.java";

        assertTrue(TestHelp.hasSonarBug(pathToFile,2259));
        TestHelp.repair(pathToFile,projectKey,2259);
        assertFalse(TestHelp.hasSonarBug(pathToFile,2259));
    }

}