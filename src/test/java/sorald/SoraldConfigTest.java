package sorald;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SoraldConfigTest {

	@Test
	public void testThatDuplicateRuleIdsAreEliminated() {
		SoraldConfig config = new SoraldConfig();
		List<Integer> ruleKeys = new ArrayList<>();
		ruleKeys.add(2116);
		ruleKeys.add(2116);
		ruleKeys.add(2184);
		ruleKeys.add(2116);
		ruleKeys.add(2184);
		config.addRuleKeys(ruleKeys);
		Assert.assertEquals(2, config.getRuleKeys().size());
	}

}
