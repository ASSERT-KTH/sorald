package sorald.sonar;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/** Class that knows about all Sonar rules. */
public class SonarRules {
    private SonarRules() {}

    /**
     * Get all SonarJava rules.
     *
     * @return All SonarJava rules.
     */
    public static Collection<SonarRule> getAllRules() {
        return Checks.getAllChecks().stream()
                .map(Checks::getRuleKey)
                .map(SonarRule::new)
                .collect(Collectors.toList());
    }

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    public static Collection<SonarRule> getRulesByType(SonarRuleType... types) {
        var ruleTypes = Set.of(types);
        return getAllRules().stream()
                .filter(rule -> ruleTypes.contains(rule.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    public static Collection<SonarRule> getRulesByType(Collection<SonarRuleType> types) {
        return getRulesByType(types.toArray(SonarRuleType[]::new));
    }
}
