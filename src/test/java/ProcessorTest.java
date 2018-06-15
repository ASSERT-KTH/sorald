import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


class ProcessorTest {

    @BeforeAll
    static void updateSonatestAnalysis()
    {
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";
        TestHelp.copy(cdtest+"src",cdrep+"src");
        TestHelp.doSonarAnalysis(cdrep);
        TestHelp.doSonarAnalysis(cdtest);
    }

    @Test
    void DeadStore()throws Exception
    {
        String pathToFile = "src/main/java/DeadStores.java";
        TestHelp.repair(pathToFile,1854);
        assertFalse(TestHelp.hasSonarBug(pathToFile,1854));
    }

}