package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import sorald.Processors;
import sorald.rule.IRuleType;
import sorald.rule.Rule;
import sorald.rule.RuleProvider;

class SonarRulesTest {
    @Test
    void getRulesByType_subsetOfRulesShouldHaveCorrectRuleType() {
        // arrange
        List<IRuleType> ruleTypes = List.of(SonarRuleType.VULNERABILITY);

        // act
        Collection<Rule> rules = RuleProvider.getRulesByType(ruleTypes);

        // assert
        rules.forEach(rule -> assertThat(rule.getType(), equalTo(SonarRuleType.VULNERABILITY)));
    }

    @Test
    void inferRules_subsetOfRulesShouldHaveACorrespondingProcessor() {
        // arrange
        List<IRuleType> ruleTypes = List.of();
        boolean handledRules = true;

        // act
        Collection<Rule> rules = RuleProvider.inferRules(ruleTypes, handledRules);

        // assert
        assertThat(rules.size(), equalTo(Processors.getAllProcessors().size()));
        rules.forEach(
                rule -> assertThat(Processors.getProcessor(rule.getKey()), is(notNullValue())));
    }
}
