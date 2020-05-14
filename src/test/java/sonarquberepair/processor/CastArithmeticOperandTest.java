package sonarquberepair.processor;

import org.junit.Test;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import sonarquberepair.Constants;
import sonarquberepair.Main;
import sonarquberepair.PrettyPrintingStrategy;
import sonarquberepair.TestHelper;

public class CastArithmeticOperandTest {

    @Test
    public void test() throws Exception {
        String fileName = "CastArithmeticOperand.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile = Constants.SONAR_WORKSPACE + "/" + Constants.SPOONED +"/" + fileName;

        JavaCheckVerifier.verify(pathToBuggyFile, new CastArithmeticOperandCheck());
        Main.main(new String[]{
                Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
                Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2184",
                Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.SNIPER.name(),
                Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,Constants.SONAR_WORKSPACE});
        TestHelper.removeComplianceComments(pathToRepairedFile);
        JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, new CastArithmeticOperandCheck());
    }

}
