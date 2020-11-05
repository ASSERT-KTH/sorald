package sorald;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(2, config.getRuleKeys().size());
    }
}
