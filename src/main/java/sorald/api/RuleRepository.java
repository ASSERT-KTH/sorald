package sorald.api;

import java.util.Collection;
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
    public Collection<Rule> getAllRules();
}
