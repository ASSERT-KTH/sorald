package sorald;

import org.junit.Test;
import org.sonar.java.checks.CastArithmeticOperandCheck;

public class FailingTest {

	@Test
	public void failing_testcase() throws Exception {
		String fileName = "MultipleProcessors.java";
		String pathToBuggyFile = "./src/test/Repo4Analyze";
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2184",
				Constants.ARG_SYMBOL + Constants.ARG_MAX_FIXES_PER_RULE,"1"});
	}

}
