package sorald;

import org.junit.Test;
import org.junit.Assert;
import org.sonar.java.checks.CastArithmeticOperandCheck;

public class FailingTest {

	@Test
	public void failing_testcase() {
		String fileName = "MultipleProcessors.java";
		String pathToBuggyFile = "./src/test/kura";
		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + fileName;

		boolean noException = true;
		try {
			Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,pathToBuggyFile,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,"2184",
				Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY, PrettyPrintingStrategy.SNIPER.name(),
				Constants.ARG_SYMBOL + Constants.ARG_MAX_FIXES_PER_RULE,"1"});
		} catch (Exception e) {
			e.printStackTrace();
			noException = false;
		}
		
		Assert.assertTrue(noException);
	}

}