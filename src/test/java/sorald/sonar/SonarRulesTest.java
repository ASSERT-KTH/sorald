package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sorald.Processors;
import sorald.rule.Rule;
import sorald.rule.RuleType;
import sorald.rule.Rules;

class SonarRulesTest {
    @Test
    void getRulesByType_subsetOfRulesShouldHaveCorrectRuleType() {
        // arrange
        List<RuleType> ruleTypes = List.of(RuleType.VULNERABILITY);

        // act
        Collection<Rule> rules = Rules.getRulesByType(ruleTypes);

        // assert
        rules.forEach(rule -> assertThat(rule.getType(), equalTo(RuleType.VULNERABILITY)));
    }

    @Test
    void inferRules_subsetOfRulesShouldHaveACorrespondingProcessor() {
        // arrange
        List<RuleType> ruleTypes = List.of();
        boolean handledRules = true;

        // act
        Collection<Rule> rules = Rules.inferRules(ruleTypes, handledRules);

        // assert
        assertThat(rules.size(), equalTo(Processors.getAllProcessors().size()));
        rules.forEach(
                rule -> assertThat(Processors.getProcessor(rule.getKey()), is(notNullValue())));
    }

    /**
     * This test is written to be aware of what rules we are scanning for. Such a test was not
     * needed before wrapping our architecture in SonarLintEngine because we used to manually pass
     * "sonar-java checks" to our engine. It is expected to fail with each version upgrade of
     * sonar-java. This is intended, however, since we can report the change of rules supported by
     * Sorald in our changelogs. If this test does not fail, that means sonar-jave does not have any
     * change in the set of rules it can scan for.
     */
    @Test
    void getAllRules_sanityCheckToKnowWhatAllRulesAreAvailableAndSoraldScansFor()
            throws IOException {
        // arrange
        Collection<String> actualRules =
                Rules.getAllRules().stream().map(Rule::getKey).collect(Collectors.toList());
        Path allRules = Paths.get("src/test/resources/all_rules.txt");
        Collection<String> expectedRules = fetchRulesFromFile(allRules);

        // assert
        assertThat(actualRules.size(), equalTo(expectedRules.size()));
        assertThat(actualRules, containsInAnyOrder(expectedRules.toArray(new String[0])));
    }

    private static List<String> fetchRulesFromFile(Path fileToBeReadLineByLine) throws IOException {
        return new ArrayList<>(Files.readAllLines(fileToBeReadLineByLine));
    }
}
