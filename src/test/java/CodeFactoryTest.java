import org.junit.Test;

public class CodeFactoryTest {

    @Test
    public void test()throws Exception
    {
        String fileName = "CodeFactory.java";
        String pathToRepairedFile = "./spooned/spoon/reflect/factory/" + fileName;
        TestHelp.normalRepair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,2116);
        TestHelp.repair(Constants.PATH_TO_FILE,Constants.PROJECT_KEY,2116);
        TestHelp.removeComplianceComments(pathToRepairedFile);
    }
}