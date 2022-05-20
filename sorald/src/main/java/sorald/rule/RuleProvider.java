package sorald.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import sorald.api.RuleRepository;

/** Entrypoint for finding available rules. */
public class RuleProvider {
    private RuleProvider() {}

    /**
     * Get all rules available to Sorald for analysis. Note that not all rules have a defined
     * repair.
     *
     * @return All rules.
     */
    public static Collection<Rule> getAllRules() {
        return ServiceLoader.load(RuleRepository.class).stream()
                .map(Provider::get)
                .map(RuleRepository::getAllRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    public static Collection<Rule> getRulesByType(IRuleType... types) {
        return ServiceLoader.load(RuleRepository.class).stream()
                .map(Provider::get)
                .map(rr -> rr.getRulesByType(types))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    public static Collection<Rule> getRulesByType(Collection<IRuleType> types) {
        return getRulesByType(types.toArray(IRuleType[]::new));
    }

    /**
     * Returns a collection of rules that are handled by Sorald.
     *
     * @return Handled rules.
     */
    public static Collection<Rule> getHandledRules() {
        return ServiceLoader.load(RuleRepository.class).stream()
                .map(Provider::get)
                .map(RuleRepository::getHandledRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all handled rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return Handled rules with any of the given types.
     */
    public static Collection<Rule> getHandledRulesByType(IRuleType... types) {
        return ServiceLoader.load(RuleRepository.class).stream()
                .map(Provider::get)
                .map(rr -> rr.getHandledRulesByType(types))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all handled rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return Handled rules with any of the given types.
     */
    public static Collection<Rule> getHandledRulesByType(Collection<IRuleType> types) {
        return getHandledRulesByType(types.toArray(IRuleType[]::new));
    }

    /**
     * Infer which rules to use based on rule types specified (or left unspecified) and their repair
     * status by Sorald on the command line.
     *
     * @param ruleTypes Types to filter rules by.
     * @param handledRules whether to fetch only handled rules.
     * @return Subset of {@link RuleRepository##getAllRules()}.
     */
    public static List<Rule> inferRules(List<IRuleType> ruleTypes, boolean handledRules) {
        if (ruleTypes.isEmpty() && !handledRules) {
            return new ArrayList<>(getAllRules());
        }
        // `handledRules` is redundant here, but I am leaving it for the sake of
        // readability.
        if (ruleTypes.isEmpty() && handledRules) {
            return new ArrayList<>(getHandledRules());
        }
        // `ruleTypes` is redundant here, but I am leaving it for the sake of
        // readability.
        if (!ruleTypes.isEmpty() && !handledRules) {
            return new ArrayList<>(getRulesByType(ruleTypes));
        }
        return new ArrayList<>(getHandledRulesByType(ruleTypes));
    }
}
