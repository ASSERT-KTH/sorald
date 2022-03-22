package sorald.sonar;

import java.util.Collection;
import java.util.stream.Collectors;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.rule.Rule;

/** Class that knows about all Sonar rules. Should ONLY be used by {@link sorald.rule.Rules}. */
public class SonarRules {
    private SonarRules() {}

    /**
     * Get all SonarJava rules.
     *
     * @return All SonarJava rules.
     */
    public static Collection<Rule> getAllRules() {
        Collection<SonarLintRuleDefinition> allRules =
                SonarLintEngine.getAllRulesDefinitionsByKey().values();

        return allRules.stream().map(slrd -> Rule.of(slrd.getKey())).collect(Collectors.toList());
    }
}
