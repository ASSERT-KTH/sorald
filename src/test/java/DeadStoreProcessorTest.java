import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


class DeadStoreProcessorTest {

    @Test
    void process()throws Exception
    {
        String pathToFile = "src/main/java/DeadStores.java";
        TestHelp.repair(pathToFile,1854);
        assertFalse(TestHelp.hasSonarBug(pathToFile,1854));
    }
}