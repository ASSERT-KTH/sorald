package sorald.api;

import java.util.Collection;
import javax.annotation.Nonnull;
import sorald.rule.IRuleType;
import sorald.rule.Rule;

/**
 * This class defines the interface for a repository of rules. It is used by Sorald to find all
 * rules to use. All implementing classes need a no-arg constructor.
 */
public interface RuleRepository {

    /**
     * Get all rules available to Sorald for analysis. Note that not all rules have a defined
     * repair.
     *
     * @return a collection of all rules. Never null
     */
    @Nonnull
    public Collection<Rule> getAllRules();

    /**
     * Get all rules matching one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    @Nonnull
    public Collection<Rule> getRulesByType(@Nonnull IRuleType... types);

    /**
     * Returns a collection of rules that are handled by Sorald.
     *
     * @return a collection of rules that are handled by Sorald.
     */
    @Nonnull
    public Collection<Rule> getHandledRules();

    /**
     * Returns a collection of rules that are handled by Sorald and match one of the given types.
     *
     * @param types Types to filter rules by.
     * @return All rules with any of the given types.
     */
    @Nonnull
    public Collection<Rule> getHandledRulesByType(@Nonnull IRuleType... types);
}
