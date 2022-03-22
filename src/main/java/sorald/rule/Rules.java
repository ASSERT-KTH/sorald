package sorald.rule;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import sorald.Processors;
import sorald.sonar.SonarRules;

/** Utility class for finding available rules. */
public class Rules {
    private Rules() {}

    /**
     * Get all rules available to Sorald for analysis. Note that not all rules have a defined
     * repair.
     *
     * @return All rules.
     */
    public static Collection<Rule> getAllRules() {
        return SonarRules.getAllRules();
    }

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    public static Collection<Rule> getRulesByType(RuleType... types) {
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
    public static Collection<Rule> getRulesByType(Collection<RuleType> types) {
        return getRulesByType(types.toArray(RuleType[]::new));
    }

    /**
     * Infer which rules to use based on rule types specified (or left unspecified) on the command
     * line.
     */
    public static List<Rule> inferRules(List<RuleType> ruleTypes, boolean handledRules) {
        List<Rule> rules =
                List.copyOf(
                        ruleTypes.isEmpty()
                                ? Rules.getAllRules()
                                : Rules.getRulesByType(ruleTypes));

        return !handledRules
                ? rules
                : rules.stream()
                        .filter(rule -> Processors.getProcessor(rule.getKey()) != null)
                        .collect(Collectors.toList());
    }
}
